# Zuul Hystrix fallback/circuit 설정

### 1. dependenct
```
<dependency>
 <groupId>org.springframework.cloud</groupId>
 <artifactId>spring-cloud-starter-hystrix</artifactId>
 <version>1.4.5.RELEASE</version>
</dependency>
```
### 2. application.yml
```
hystrix:
threadpool:
 default:
   coreSize: 100  # Hystrix Thread Pool default size
   maximumSize: 500  # Hystrix Thread Pool default size
   keepAliveTimeMinutes: 1
   allowMaximumSizeToDivergeFromCoreSize: true
command:
 default:
   execution:
     isolation:
       thread:
         timeoutInMilliseconds: 3000     #설정 시간동안 처리 지연발생시 timeout and 설정한 fallback 로직 수행
   circuitBreaker:
     requestVolumeThreshold: 2                # 설정수 값만큼 요청이 들어온 경우만 circut open 여부 결정 함
     errorThresholdPercentage: 50        # requestVolumn값을 넘는 요청 중 설정 값이상 비율이 에러인 경우 circuit open
     enabled: true
```
### 3. fallbackmethod
```
public class CustomFallbackProvider implements FallbackProvider {
	 private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CustomFallbackProvider.class);
	
	 private static final String SERVICE_ID = "serviceId";
	 private static final String REQUEST_URI = "requestURI";
	 
	 @Value("${hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds}")
	 private String hystrixThreadTimeoutMilliseconds;
	 
	// 모든 route에 대해 기본 fallback을 지정하고 싶으면, "*"이나 null을 반환
	 @Override
	 public String getRoute() {
		 return "*";
		 //return "customers"; //라우팅 path별로 별도로 지정할때
     }

	 private ClientHttpResponse response(final String errType, final String errMsg) {
		 return new CustomClientHttpResponse(errType, errMsg) ;
	 }      
     private ClientHttpResponse response(final HttpStatus status) {
        return new CustomClientHttpResponse(status) ;
    }

	@Override
	public ClientHttpResponse fallbackResponse(Throwable cause) {
		cause.printStackTrace();
		LOGGER.debug("[CustomFallbackProvider] cause:"+cause.getMessage());
		
		if(cause!=null && cause.getMessage().indexOf(ApiCommon.RIBBON_SERVICE_UNAVAILABLE)!=-1) {
			return response(HttpStatus.SERVICE_UNAVAILABLE);
		}else {
			if (cause instanceof HystrixTimeoutException) {
	            return response(HttpStatus.GATEWAY_TIMEOUT);
	        }else if (cause instanceof HystrixRuntimeException) {
	        	HystrixRuntimeException hyRunExp = (HystrixRuntimeException) ((HystrixRuntimeException) cause).getFallbackException();
	        	FailureType fType = hyRunExp.getFailureType();
	        	LOGGER.debug("[CustomFallbackProvider] fType:"+fType.name().toString());	    		
	        	return response(String.valueOf(ApiCommon.GATEWAY_RUNTIME_ERROR), hyRunExp.getMessage());
	        }else if (cause instanceof HystrixBadRequestException) {
	            return response(HttpStatus.BAD_GATEWAY);
	        } else {
	            return response(HttpStatus.INTERNAL_SERVER_ERROR);
	        }
		}
	}
	
		@Override
		public ClientHttpResponse fallbackResponse() {
			return null;
		}
}

public class CustomClientHttpResponse implements ClientHttpResponse{
	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CustomFallbackProvider.class);
	
	private HttpStatus status;
	private String errType;
	private String errMsg;
	public CustomClientHttpResponse(HttpStatus status) {
		this.status = status;
	}
	public CustomClientHttpResponse(String errType, String errMsg) {
		this.errType = errType;
		this.errMsg = errMsg;
	}
	
	@Override
    public HttpStatus getStatusCode() throws IOException { return status; }
	@Override
	public int getRawStatusCode() throws IOException { return status.value(); }
	@Override
	public String getStatusText() throws IOException { return status.getReasonPhrase(); }
	@Override
	public void close() {      }

	@Override
    public InputStream getBody() throws IOException {
		HttpStatus status = getStatusCode();
		JSONObject resData = new JSONObject();        	
		
		if(status!=null) {
			LOGGER.info("[CustomFallbackProvider] status:" + getStatusCode()+",reason:"+getStatusText());
    		
			resData.put("ResultTypeCode", String.valueOf(status));
			resData.put("ErrorDescription", getStatusText());
		}else {
			LOGGER.info("[CustomFallbackProvider] errType:" + errType+",errMsg:"+errMsg);
			
			resData.put("ResultTypeCode", errType);
			resData.put("ErrorDescription", errMsg);
		}
		
        return new ByteArrayInputStream(resData.toJSONString().getBytes());
    }

	@Override
    public HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}


public class CoeZuulApplication {

 public static void main(String[] args) {
 SpringApplication.run(CoeZuulApplication.class, args);
 }

 @Bean
 public FallbackProvider zuulFallbackProvider() {
 return new CustomFallbackProvider();
 }
}
```


