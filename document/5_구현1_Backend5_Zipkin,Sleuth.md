
# Zipkin,Sleuth 연계를 통한 Request별 추적기능 강화

### 0. Zuul Gateway 연동: Zuul에서 생성한 uuid를 Slueth Span의 TraceId로 지정하여 시스템연계  

- 참고:  https://brunch.co.kr/@springboot/58

### 1.구성: 집킨-서버, 킵킨 클라이언트(zipkin-client, slueth)
### 2.open source(github):  
 > https://github.com/openzipkin/zipkin
 > 집킨서버 설정:  https://github.com/openzipkin/zipkin/blob/master/zipkin-server/src/main/resources/zipkin-server-shared.yml

### 3.설치(방법 3가지): 
- jar실행
- 도커실행
- spring boot 소스구동(spring boot로 직접 구성하지말고, 2번 레파지토리에서 받아서 구동하는게 답일듯)
>로컬에 테스서버로 구동시  1번 방법 추천
>참고: https://zipkin.io/pages/quickstart 

### 4.서버 구성 및 실행

#### 4-1)킵킨서버 로컬 실행: java -jar zipkin.jar

#### 4-2) 클라이언트: 
> dependency 추가(gradle)
```
  implementation 'org.springframework.cloud:spring-cloud-starter-sleuth'
  implementation 'org.springframework.cloud:spring-cloud-starter-zipkin'
```

#### 4-3)킵킨서버 설정
- 대시보드: http://hostip:9411(default)
- 로그설정
>  https://github.com/openzipkin/zipkin/issues/2798
- 모든 서버로그 보기(Http Request 로그는 메모리 저장소 설정에서 출력안됨)
> https://github.com/openzipkin/zipkin/issues/2798
```
java -Dlogging.level.ROOT=TRACE -jar zipkin.jar
```
- 메모리 설정(jvm 메모리 설정)
```
java -Xmx1G -jar zipkin.jar
```

### 5.TraceID 생성 및 inject
> 참고1:  https://cloud.spring.io/spring-cloud-sleuth/1.3.x/multi/multi__customizations.html
> 참고2:  https://blog.michaelstrasser.com/2017/07/using-sleuth-trace-id/

```
public class CustomHttpSpanExtractor implements HttpSpanExtractor {
        private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CustomHttpSpanExtractor.class);


        private final Random random = new Random(System.currentTimeMillis());
        //todo. api마다 span name을 분리할수 있음
        public static final String SPAN_NAME_BASE = "SLEUTH.TRACE";
        
        @Override
        public Span joinTrace(SpanTextMap carrier) {
                Map<String, String> map = TextMapUtil.asMap(carrier);             
                
                RequestContext ctx = RequestContext.getCurrentContext();
                
                String uuid = ApiUtil.getRandomUUIDStr().replace("-", ""); // '-'제거
        ctx.put("uuid", uuid);
        Span newSpan = spanForId(uuid);
        ctx.put("traceId", newSpan.traceIdString());
        
                // extract all necessary headers
                Span.SpanBuilder builder = Span.builder()
                                .traceIdHigh(newSpan.getTraceIdHigh())
                                .traceId(newSpan.getTraceId())
                                //gateway에서는 traceid와 spainId가 같은 값임, 32byte중 16byte사용(연산복잡성...)
                                .spanId(newSpan.getSpanId())    
                                .name(newSpan.getName());
                
                return builder.build();
        }
        
        private Span spanForId(String uuid) {   
        return Span.builder()
                        .traceIdHigh(Span.hexToId(uuid.substring(0,16)))
                .traceId(Span.hexToId(uuid.substring(16)))
                //traceID가 같은 상황에서 spanID는 16byte여도 상관없음
                .spanId(Span.hexToId(uuid.substring(0,16)))
                .exportable(false)
                .name(SPAN_NAME_BASE)
                .build();
    }
```


* 추가 hystrix Command 에서 발생하는 자동 TraceID passing 필요(zipkin서버에 초단위로 남음)
 >  https://cloud.spring.io/spring-cloud-static/Greenwich.SR3/multi/multi__integrations.html
  : zuu과 개별서비스에 아래 설정(application.properties)적용
```
spring.sleuth.rxjava.schedulers.ignoredthreads = true
spring.sleuth.rxjava.schedulers.hook.enabled = false
```
