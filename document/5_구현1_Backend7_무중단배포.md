# 무중단 배포(Zero Downtiome deployment)

### 적용방안
#### 1. 문제점: 재기동(도커 down,run)간 서비스가 종료된 이후, 또는 정상 구동후 처리준비가 완료된 이전에 트래픽이 전달되어 "Service Unavailable(503)" 발생
#### 2. 개선방안1-개별서비스:
- 서비스 down시: Eureka서버로 미리 서비스 down을 통지(1) + Eureka목록에서 제거(2) + 톰캣서버에 쌓여있는 request 처리할때까지 대기(3) + 서버종료  
- 서비스 up시: 서비스 구동직후 -> 서비스상태 Starting((1), Up상태가 아니면 처리불가상태) →  DB초기화(2) →  JVM기동완료까지 대기(3) → 자기자신으로 Request 샘플전송((4), 정상처리여부확인 + 잔여초기화수행) -> 서비스 up 통지(5)  
#### 개선방안2- 게이트웨이(zuul)
- 문제점: 서버 재기동간에 nginx웹서버에서 트래픽을 계속 전송하여 문제가됨  
- 개선방안: zuul 서버 재기동시, 웹서버(nginx)가 상태정보를 인식하고 트랙픽 차단하도록 함.  
  nginx_upstream_check_module로 patch하고 nginx를 컴파일해서 적용함 → Health Check기능 사용할수 있음(Nginx에서 Health_Check모듈은 상용버전에서 사용가능).  
  도커파일 대신 nginx서버를 직접 서버에 설치해서 사용.  
  
### 샘플코드

#### 서비스 down시
- 서버 entry point
```
//shutdown 이벤트 등록
@SpringBootApplication
.. {
  @Bean
  public ServletContainerEventHandler servletContainerEventHandler() {
    return new ServletContainerEventHandler(new GracefulShutdown());
  }
}
```
- JVM 다운시 Event Handler 등록
```
public class ServletContainerEventHandler implements EmbeddedServletContainerCustomizer {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServletContainerEventHandler.class);
  private GracefulShutdown gracefulShutdown;
  public ServletContainerEventHandler(GracefulShutdown gracefulShutdown) {
    this.gracefulShutdown = gracefulShutdown;
  }

  @Override
  public void customize(ConfigurableEmbeddedServletContainer container) {
    LOGGER.info("[ServletContainerEventHandler] regist ConfigurableEmbeddedServletContainer");
    if (container instanceof TomcatEmbeddedServletContainerFactory) {
      ((TomcatEmbeddedServletContainerFactory) container)
      .addConnectorCustomizers(gracefulShutdown);
    }
  }
}
```

- Shutdown시 톰캣 Request를 처리할때까지 대기후 종료
```
//참고: https://blog.marcosbarbero.com/graceful-shutdown-spring-boot-apps/
@Component
@RefreshScope
public class GracefulShutdown implements TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent> {
  private static final Logger LOGGER = LoggerFactory.getLogger(GracefulShutdown.class);
  private static final int TIMEOUT = 10;
  private volatile Connector connector;
  @Autowired
  private EurekaConnector eurekaConn;
  //lease-renewal-interval-in-seconds 값으로 대체 요망
  @Value("${deploy.shutdown.time}")
  private int shutdownTime=10; //default 10->config 15초이상 설정

  @Override
  public void customize(Connector connector) {
  `this.connector = connector;
  }

  //kill pid로 동작함
  @Override
  public void onApplicationEvent(ContextClosedEvent event) {
    LOGGER.info("[GracefulShutdown] onApplicationEvent start! ");
    //0. health Checker에서 사용할 변수 업데이트 -> eureka 서버로 renew할때 서비스 상태가 반영되도록 함
    RunningStatus.serverStatus = InstanceStatus.DOWN;
    //EUREKA REST API(PUT=DOWN)를 사용해도 일정시간동안 트래픽 차단 및 정상동작이 안되 skip함
    //1. Eureka 서버에 down명령어를 날림
    eurekaConn.deleteServiceDeregist();
    //2. 10초 대기(설정값에 따라다름, 유레카서버 재반영 주기등 고려)
    //lease-renewal-interval-in-seconds + 5초 정도 더 대기시간 필요
    try {
      for(int i=0; i<shutdownTime; ++i) {
        Thread.sleep(1000);
        LOGGER.info("[GracefulShutdown] shutdown ready...");
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    //3. 톰캣 Request 쓰레드 종료시까지 대기 및 프로세스 종료
    LOGGER.info("[GracefulShutdown] onApplicationEvent > do stop! ");
    stopProcess();
  }
  public void stopProcess() {
    this.connector.pause();
    Executor executor = this.connector.getProtocolHandler().getExecutor();
    if (executor instanceof ThreadPoolExecutor) {
      try {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
        threadPoolExecutor.shutdown();
        if (!threadPoolExecutor.awaitTermination(TIMEOUT, TimeUnit.SECONDS)) {
          LOGGER.warn("Tomcat thread pool did not shut down gracefully within "
          + TIMEOUT + " seconds. Proceeding with forceful shutdown");

          threadPoolExecutor.shutdownNow();

        if (!threadPoolExecutor.awaitTermination(TIMEOUT, TimeUnit.SECONDS)) {
          LOGGER.error("Tomcat thread pool did not terminate");
          }
        }
      } catch (InterruptedException ex) {
        ex.printStackTrace();
        LOGGER.error("stopProcess err:" + ex.getMessage());
        Thread.currentThread().interrupt();
      }
    }
  }
}
```

- 공통(Down,Up): Eureka에게 서버상태를 통지
```
@Service
@RefreshScope
public class EurekaConnector {
  private static final Logger LOGGER = LoggerFactory.getLogger(EurekaConnector.class);
  private final RestTemplate restTemplate = new RestTemplate();
  private String EUREKA1_SERVER_URL = "";
  private String EUREKA2_SERVER_URL = "";
  private String URI_PARAM_DOWN = "status?value=DOWN"; //or OUT_OF_SERVICE
  private String URI_PARAM_UP = "status?value=UP";

  @Value("${eureka1.server.ip}")
  private String eureka1Ip;
  @Value("${eureka1.server.port}")
  private String eureka1Port;
  @Value("${eureka2.server.ip}")
  private String eureka2Ip;
  @Value("${eureka2.server.port}")
  private String eureka2Port;
  @Value("${spring.application.name}")
  private String serviceName;
  @Value("${aircargotrace.server.port}")
  private String servicePort;
  public EurekaConnector(){}
  @PostConstruct
  public void setParamInfo() throws UnknownHostException {
    String hostName = InetAddress.getLocalHost().getHostName(); //msa-poc, msa-poc2
    EUREKA1_SERVER_URL = "http://" + eureka1Ip + ":" + eureka1Port + "/eureka/apps/"
    + serviceName.toUpperCase() + "/"
    + hostName + ":" + serviceName + ":" + servicePort + "/";
    EUREKA2_SERVER_URL = "http://" + eureka2Ip + ":" + eureka2Port + "/eureka/apps/"
    + serviceName.toUpperCase() + "/"
    + hostName + "2" + ":" + serviceName + ":" + servicePort + "/";
  }
  //ex: http://ip:port/eureka/apps/AIRCARGOTRACE.DO/msa-poc:airCargoTrace.do:8101/status?value=DOWN
  //모든 유레카 서버에게 상태 변경요청
  public void putServiceStatus(String status) {
    String uri_param_status = null;
    if("DOWN".equals(status)) {
      uri_param_status = URI_PARAM_DOWN;
    }else {
      uri_param_status = URI_PARAM_UP;
    }
    try {
      String url1 = EUREKA1_SERVER_URL + uri_param_status;
      LOGGER.info("[EurekaConnector] putServiceDown url : {}", url1);
      restTemplate.put(url1, null);
    }catch(Exception e) {
      LOGGER.info("[EurekaConnector] putServiceDown err : {}", e.getMessage());
    }
    try {
      String url2 = EUREKA2_SERVER_URL + uri_param_status;
      LOGGER.info("[EurekaConnector] putServiceDown url : {}", url2);
      restTemplate.put(url2, null);
    }catch(Exception e) {
      //404 null 에러가 떨어지지만, 이렇게 해야 server-down상태임이 처리됨
      LOGGER.info("[EurekaConnector] putServiceDown err : {}", e.getMessage());
    }
  }
  public void deleteServiceDeregist() {
    try {
      String url1 = EUREKA1_SERVER_URL;
      LOGGER.info("[EurekaConnector] deleteServiceDeregist url : {}", url1);
      restTemplate.delete(url1);
    }catch(Exception e) {
      LOGGER.info("[EurekaConnector] deleteServiceDeregist err : {}", e.getMessage());
    }
    try {
      String url2 = EUREKA2_SERVER_URL;
      LOGGER.info("[EurekaConnector] deleteServiceDeregist url : {}", url2);
      restTemplate.delete(url2);
    }catch(Exception e) {
      //404 null 에러가 떨어지지만, 이렇게 해야 server-down상태임이 처리됨
      LOGGER.info("[EurekaConnector] deleteServiceDeregist err : {}", e.getMessage());
    }
  }
}
```

#### 4-2) 서비스 Up시

- 서버 entry point
```
@SpringBootApplication
.. {
  @PostConstruct
  public void PostInit() {
    postJobThread.start();
  }
}
```

- PostJob Thread : 초기화 및 rest 샘플전송
```
@Component
@RefreshScope
public class ServerStartPostJob extends Thread {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerStartPostJob.class);
  @Autowired
  private DBRefreshController dbRefreshCtrl;
  @Value("${aircargotrace.server.port}")
  private String serverPort;
  @Autowired
  private EurekaConnector eurekaConn;
  @Override
  public void run() {
    LOGGER.info("[ServerStartPostJob] PostInit start!");
    //1. Eureka service=STARTING 처리
    RunningStatus.serverStatus = InstanceStatus.STARTING;
    //2. DB초기화
    dbRefreshCtrl.refreshLimit();
    //JVM기동후 rest sample 확인을 위해 대기
    try {
      while(true) {
        Thread.sleep(1000);
        LOGGER.info("[ServerStartPostJob] standby... ");
        if(RunningStatus.JVM_RUNNING.equals(RunningStatus.JvmRunningComplete)) {
          LOGGER.info("[ServerStartPostJob] RunningStatus.JVM_RUNNING ");
          break;
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    //3. 자기자신으로 정상요청 한건 날린후 up처리하기
    HttpStatus res = requestSimpleTest();
    if(res!=HttpStatus.OK) {
      //todo. 200 OK가 아닐때 프로세스 종료 또는 재확인 등 로직추가 가능
    }
    //4. Eureka service=UP 처리
    eurekaConn.putServiceStatus("UP");
    RunningStatus.serverStatus = InstanceStatus.UP;
    LOGGER.info("[ServerStartPostJob] PostInit end!");
  }

  private HttpStatus requestSimpleTest(){
    RestTemplate restTemplate = new RestTemplate();
    String hostIp = null;
    try {
      hostIp = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      e.printStackTrace();
      System.exit(0);
    }
    String selfUrl = "http://" + hostIp + ":" + serverPort + ApiCommon.API_URL;
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    StringBuffer cookieSB = new StringBuffer();
    cookieSB.append("uuid=").append(ApiUtil.getRandomUUID().replace("-", "")+ ";");
    cookieSB.append("apiId=").append(ApiCommon.API_ID+ ";");
    headers.add("Cookie", cookieSB.toString());
    headers.add(ApiCommon.API_HEADER_USER_ID, "FFTEST0001");

    JSONObject reqJson = new JSONObject();
    JSONArray resultList = new JSONArray();
    JSONObject responseJson = new JSONObject();
    responseJson.put("data1", "value1");
    resultList.add(responseJson);
    reqJson.put("RequestList", resultList);
    String reqBody = reqJson.toJSONString();
    LOGGER.info("[ServerStartPostJob] reqBody:" + reqBody);
    ResponseEntity<String> res = null;
    try {
      res = restTemplate.exchange(selfUrl, HttpMethod.POST, new HttpEntity<String>(reqBody, headers), String.class);
      LOGGER.info("[ServerStartPostJob] response:" + res);
    }catch(Exception e) {
      LOGGER.info("[ServerStartPostJob] res err:" + e.getMessage());
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    return res.getStatusCode();
  }
}
```

- 공통(down,up): Health Check Component  
 > 설정한 시간간격(10초)으로 Eureka서버에 서버상태를 알림(명시적 호출외에 이중으로 추가 처리기능 설정함)
```
@Component
public class ServiceHealthCheckHanlder implements HealthCheckHandler{
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceHealthCheckHanlder.class);
  @Override
  public InstanceStatus getStatus(InstanceStatus currentStatus) {
    if(InstanceStatus.STARTING.equals(RunningStatus.serverStatus)) {
      LOGGER.info("[ServiceHealthCheckHanlder] getStatus: {} -> {} ", currentStatus, RunningStatus.serverStatus);
      currentStatus = InstanceStatus.STARTING; //서버 구동후, db초기화 이전까지 staring으로 유지
    }else if(InstanceStatus.DOWN.equals(RunningStatus.serverStatus)) {
      LOGGER.info("[ServiceHealthCheckHanlder] getStatus: {} -> {} ", currentStatus, RunningStatus.serverStatus);
      currentStatus = InstanceStatus.DOWN; //shutdown 수행시 DOWN처리
    }else if(InstanceStatus.UP.equals(RunningStatus.serverStatus)) {
      if(currentStatus!=InstanceStatus.UP) {
        LOGGER.info("[ServiceHealthCheckHanlder] getStatus: {} -> {} ", currentStatus, RunningStatus.serverStatus);
      }
      currentStatus = InstanceStatus.UP; //서버 구동후, db초기화 이후 UP상태로
    }else {
      currentStatus = InstanceStatus.UNKNOWN;
    }
    return currentStatus;
  }
}
```

#### Zuul Health Check → Nginx 호출
- zuul health 체크용 controller
```
@Controller
public class NginxHealthCheker {
  private static final Logger LOGGER = LoggerFactory.getLogger(EurekaConnector.class);
  @Autowired(required=false)
  private HealthEndpoint healthEndpoint;

  @RequestMapping("/zuul_health")
  @ResponseBody
  @ConditionalOnBean(value={HealthEndpoint.class})
  public ResponseEntity<String> zuulHealthCheck() {
    Health health = healthEndpoint.invoke();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    //200 or 503 상태코드로 상태체크
    if(InstanceStatus.UP.equals(RunningStatus.serverStatus)) {
     //LOGGER.info("[NginxHealthCheker] zuulHealthCheck : " + HttpStatus.OK);
      return new ResponseEntity<String>("", headers, HttpStatus.OK);
    } else {
     LOGGER.info("[NginxHealthCheker] zuulHealthCheck : " + HttpStatus.SERVICE_UNAVAILABLE);
      return new ResponseEntity<String>("", headers, HttpStatus.SERVICE_UNAVAILABLE);
    }
  }
}
```
- nginx health_check 적용코드
```
upstream zuul_server{
  least_conn;
  server ip1:port1; # slow_start=10s;
  server ip2:port2; # slow_start=10s;

  check interval=3000 rise=5 fall=1 timeout=1000 type=http;
  check_http_send "GET /zuul_health HTTP/1.0\r\n\r\n";
  check_http_expect_alive http_2xx;
}

server {
  listen 80;
  server_name msa-poc;

  # all path
  location ~ $ {
    proxy_pass http://zuul_server;
    #health_check uri=/zuul_health; #상업용
  }
}
```
