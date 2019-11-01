- 참고:  https://jsonobject.tistory.com/464


### [zuul gateway server]

![image](https://user-images.githubusercontent.com/45334819/60981599-9c465e00-a371-11e9-9af1-d140273efe52.png)

- 필요성
 - API Gateway 패턴을 도입하면 마이크로서비스 간에 반복적으로 발생하는 인증과 로그 모니터링과 같은 공통 기능을 중앙으로 단일화할 수 있다.
 - API Gateway는 회사 내부의 마이크로서비스 간에만 필요한 것이 아니다. 외부 써드파티와 연동시 기존 마이크로서비스의 수정 없이 제공할 API만 선별하여 노출시킬 수 있다. 즉, 서로를 직접 노출 없이 격리함으로서 보안 안정성을 높일 수 있다.

### - Zuul 서버 설정

#### 1. dependency
```groovy
ext {
    set('springCloudVersion', 'Greenwich.RELEASE')}

dependencies {
    implementation('org.springframework.cloud:spring-cloud-starter-netflix-zuul')
    implementation('org.springframework.cloud:spring-cloud-starter-netflix-ribbon')
    testImplementation 'org.springframework.boot:spring-boot-starter-test'}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
    }
}
```

#### 2. java annotaion
```java
@SpringBootApplication@EnableZuulProxypublic class ZuulDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZuulDemoApplication.class, args);
    }
}
````

#### 3)application.yml 
- 기본설정
``` yml
spring:
  application:
    name: api-gateway-demo
#config...
#Eureka...

server:
  port: 8080

zuul:
  routes:
    foo-api:
      path: /foos/**
      url: http://localhost:8081
      stripPrefix: false
    bar-api:
      path: /bars/**
      url: http://localhost:8082
      stripPrefix: false
```

- msap-zuul-server 예제
``` yml
#8000~01:config, 8002~8009:Eureka, 8010~19:zuul, 8020~24:인증, 8025~29:Redis, 8100~:ms
server:
  port: ${zuul.server.port}
#8010

#Eureka Client   
eureka:
  instance:
    prefer-ip-address: true
  client:
    serviceUrl:
#      defaultZone: http://210.102.77.219:8002/eureka/
      defaultZone: http://${eureka.server.ip}:${eureka.server.port}/eureka/
    register-with-eureka: true
    fetch-registry: true

#/actuator/routes(But no-routes, because no-properties file set)
#to print routes list, add RestController and modify zuul-jdbc
management:
  security:
    enabled: false
  endpoints:
    web:
      exposure:
        include: '*'  # info, health, routes, application, application/routes, actuator/refresh

#Zuul-Oracle-Jdbc Enable
zuul:
  store:
    jdbc:
      enabled: true
    manual:
      refresh: true
#  ignored:
#    services : '*'

#actuator api 확인정보
#라우팅목록: http://210.102.77.219:8010/routes
#라우팅 상세정보: http://210.102.77.219:8010/routes?format=details
```

<hr />

### [zuul filter]


##### 1. PRE Filter - 라우팅전에 실행되며 필터이다. 주로 logging, 인증등이 pre Filter에서 이루어 진다.  
##### 2. ROUTING Filter - 요청에 대한 라우팅을 다루는 필터이다. Apache httpclient를 사용하여 정해진 Url로 보낼수 있고, Neflix Ribbon을 사용하여 동적으로 라우팅 할 수도 있다.  
##### 3. POST Filter - 라우팅 후에 실행되는 필터이다. response에 HTTP header를 추가하거나, response에 대한 응답속도, Status Code, 등 응답에 대한 statistics and metrics을 수집한다.  
##### 4. ERROR Filter - Routing에러 또는 Micro-Service 에러 발생시 실행되는 필터이다.  
##### 5. Custom Filter - Pre Filter의 오류발생시 Custom Filter로 받아서 응답처리  

#### - 필터처리 순서 처리순서
1)정상: Pre filter -> Routing Filter -> Post Filter  
2)Pre Filter 오류: Pre Filter -> Customs Filter -> Error Filter-> Post Filter  
3)Route Filter 또는 MicroService오류 : Pre Filter -> Routing Filter -> Error Filter -> Post Filter  


#### - 필터별 역할 (예)
1)Pre Filter         : 인증, 로깅  
2)Customs Filter: 인증 오류응답 처리   
3)Error Filter      :  라우팅/MS오류 오류응답 처리  
4)Post Filter       : 동시접속 관리  


<hr />

### [Zuul 환경설정]  
#### 1. 라우팅: PathMatching /aaa/path**로 처리하는 방법
- 기본: 레퍼런스에서는 /aaa/path/** 패턴으로 /**로 끝나야함
- 개선: /**대신 알파벳**로 끝날시, RoutingFilter에서 "/" + substr(1, serviceId 길이)로 처리해주면 됨(단. prefix 제거 조건에서)  

#### 2. SensitiveHeader(민감헤더 제거) 설정  

1. 기본 properties 파일설정 방식:
```
zuul:
routes: #=> zuul-jdbc로 db테이블에 정의함
   serviceName:
     sendsitiveHeader:
```
2. zuul-jdbc 설정
- 컬럼명: zuul_routes.SENSITIVE_HEADERS
3.설명
- sendsitiveHeader 설정시, 설정한 민감헤더를 제거함
- 단, zuul은 기본적으로 Cookie, Set-Cookie, Autorization은 통과시팀

4. 설정방법
4-1. sendsitiveHeader : 미설정 > 전체 통과시킴
4-2. sendsitiveHeader : 'ULH-Api-Id'
- client가 보내는 헤더정보에서 해당정보가 downstream으로 전달되지 않는다.(제거)

5.확인된 오류
- zuul-jdbc설정해서 발생하는 문제인지는 확인되지 않았으나,
- sendsitiveHeader : Cookie, Set-Cookie, Autorization을 설정하여도 cookie정보가 제거되지 않고 전달됨을 확인함  








