# Eureka 설정 및 HA구성(zone) 설정

### 1. Eureka 설정

- 참고: https://coe.gitbook.io/guide/service-discovery/eureka  
```
server:
  port: ${eureka1.server.port} #8002
#8000~01:config, 8002~8009:Eureka, 8010~19:zuul, 8020~24:인증, 8025~29:Redis, 8100~:ms

#Eureka Standalone Server       
eureka:
  client:
    serviceUrl:
      #유레카서버는 config서버와 별개로 동작하게 하기위해 dockerfile에서 받아옴
      #이중화된 상대방 서버를 등록함(A->B, B->A), 유레카 서버는 이중화서버 하나씩 등록하고, client는 모든 유레카 서버를 등록함  
      defaultZone: http://${eureka1.server.ip}:${eureka1.server.port}/eureka/
#      defaultZone: http://localhost:8002/eureka/
    registerWithEureka: true # 유레카 이중화시 서버 등록여부를 확인하기위해 유레카 자신도 서버목록으로 등록
    fetchRegistry: true      # defaultZone의 유레카 서버에서 클라이언트 정보를 가져온다
    healthcheck:
      enabled: true
#Eureka Client Cache 설정
#Eureka client에 존재하는 cache로 eureka server에 서비스 정보 요청 시 이 cache의 값을 이용 한다.   
#eureka.client.fetchRegistry 값이 false이면 client cache는 적용되지 않는다.   
    registryFetchIntervalSeconds: 10 # 기본 30초
    preferSameZoneEureka : true #클러스터링된 Eureka구성에서 나의 Zone의 Client를 우선할것인지
  instance:
    hostname: ${HOST_NAME}
    metadataMap:
      zone : ${spring.profiles.active} #dev1
    prefer-ip-address: false
    status-page-url-path: http://${eureka1.server.ip}:${eureka1.server.port}/info
    health-check-url-path: http://${eureka1.server.ip}:${eureka1.server.port}/health
    #Client는 eureka에 등록 이후 설정된 주기마다 heatbeat를 전송하여 자신의 존재를 알림
    lease-renewal-interval-in-seconds: 10 #(default: 30)
    #설정된 시간동안 heartbeat를 받지 못하면 해당 Eureka Instance를 Registry에서 제거
    lease-expiration-duration-in-seconds: 30 #(default: 90)
    initial-status: STARTING     #instance-enabled-onit를 사용하기 위한 초기상태 지정
    instance-enabled-onit: true  #traffic 받을 준비가 되었을때 UP상태로 바꿈
#Eureka Server Response Cache 설정
#Eureka server에서 eureka client에게 자신의 registry 정보를 제공 시 사용하는 cache.  
#client에게 더 빠른 registry 정보 제공을 위해 실제 registry 값이 아닌 cache의 값을 제공 함.  
  server:
    response-cache-update-interval-ms: 10000 # 기본 30초
#Self-Preservation Mode(자가보존모드)
#Eureka로의 네트워크는 단절되었지만, 해당 서비스 API를 호출하는데 문제가 없는 경우가 있을수 있어서,
#self-preservation 을 사용하여 registry에서 문제된 instance를 정해진 기간 동안 제거하지 않을 수 있다.
#  server:
    enable-self-preservation: true #설정3가지: none(유레카 서버간 동기화등 특정동작있음)/true/false
    
#Ribbon cache 설정(cache를 정리하며 ribbon cache 도 포함하여 정리 함)
#Zuul, Feign 에서 다른 서비스 호출 시 사용 되는 cache
ribbon:
  ServerListRefreshInterval: 3000 #ServerListRefreshInterval
  
management:
  security:
    enabled: false
  endpoints:
    web:
      exposure:
        include: '*'
endpoints:
  actuator:
    enabled: true
  shutdown:
    enabled: true # eureka에 종료를 알리고 서버 down: ip:port/shutdown

#for print color
spring:
  output:
    ansi:
      enabled: always
logging:
  path: /logs
  file: ${spring.application.name}
```

<br>

### 2. HA구성

#### 2-1. Eureka Server 설정

- Peer 가 되는 IP 와 Port 정보를 defaultZone 을 수정
- discovery-service 1 (IP : 1.1.1.1)
```
server:
  port: 8061
 
eureka:
  instance:
    hostname: localhost
  client:
    registerWithEureka: false
    fetchRegistry: false
    serviceUrl:
#      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
       defaultZone: http://2.2.2.2:${server.port}/eureka/
``` 

- discovery-service 2 (IP : 2.2.2.2)  
```
server:
  port: 8061
 
eureka:
  instance:
    hostname: localhost
  client:
    registerWithEureka: false
    fetchRegistry: false
    serviceUrl:
#      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
       defaultZone: http://1.1.1.1:${server.port}/eureka/
``` 

### 2-2. Eureka Client 설정   
- proxy-service, A-service, B-servie 모두 추가 한다.  
```
server:
  port: 8060
 
eureka:
  client:
    serviceUrl:
      defaultZone: http://1.1.1.1:8061/eureka/, http://2.2.2.2:8061/eureka/
```

![image](https://user-images.githubusercontent.com/45334819/71929093-39c50200-31dc-11ea-83c3-40788b75d5e9.png)  
