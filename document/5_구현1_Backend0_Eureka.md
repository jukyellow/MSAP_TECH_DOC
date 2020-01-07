# Eureka 설정 및 HA구성(zone) 설정

### 1. Eureka 설정

- 참고: https://coe.gitbook.io/guide/service-discovery/eureka  
```
eureka:
  client:
    serviceUrl:
      defaultZone: http://192.168.1.19:8761/eureka/
      enabled: true
    register-with-eureka: true   #다른 서비스가 자신을 발견할 수 있도록 자신의 정보를 Eureka에 등록
    fetchRegistry: true          #defaultZone의 유레카 서버에서 클라이언트 정보를 가져온다(registerWithEureka가 true로 설정되어야 동작함)
    instance-info-replication-interval-seconds: 30 #전에 받은 registry를 갱신하기 위해 기본 30초마다 Eureka에 fetch REST 요청을 실행
  instance:
    preferIpAddress: true # 서비스간 통신 시 hostname 보다 ip 를 우선 사용 함

eureka:
  server:
    #Eureka Server도 Eureka Client로 동작하기 때문에 Eureka HA 환경에서 Peer node에 자기 자신을 등록하고 Registry sync를 맞추기 위해 replication 작업을 수행함
    registry-sync-retrires: 5
    enable-self-preservation: true
#Eureka로의 네트워크는 단절되었지만, 해당 서비스 API를 호출하는데 문제가 없는 경우가 있을수 있어서,
#self-preservation 을 사용하여 registry에서 문제된 instance를 정해진 기간 동안 제거하지 않을 수 있다.

#Eureka Server Response Cache 설정
#Eureka server에서 eureka client에게 자신의 registry 정보를 제공 시 사용하는 cache.  
#client에게 더 빠른 registry 정보 제공을 위해 실제 registry 값이 아닌 cache의 값을 제공 함.  
eureka.server.response-cache-update-interval-ms: 3000 # 기본 30초

#Eureka Client Cache 설정
#Eureka client에 존재하는 cache로 eureka server에 서비스 정보 요청 시 이 cache의 값을 이용 한다.   
#eureka.client.fetchRegistry 값이 false이면 client cache는 적용되지 않는다.   
eureka.client.registryFetchIntervalSeconds: 3 # 기본 30초

#Ribbon cache 설정(cache를 정리하며 ribbon cache 도 포함하여 정리 함)
#Zuul, Feign 에서 다른 서비스 호출 시 사용 되는 cache
ribbon.ServerListRefreshInterval: 3000

##Renew
#Client는 eureka에 등록 이후 설정된 주기마다 heatbeat를 전송하여 자신의 존재를 알림
eureka.instance.lease-renewal-interval-in-seconds (default: 30)
#설정된 시간동안 heartbeat를 받지 못하면 해당 Eureka Instance를 Registry에서 제거
eureka.instance.lease-expiration-duration-in-seconds (default: 90)

##Peering
#Fetch Registry
eureka.client.registryFetchIntervalSeconds (default: 30)

#Standalone으로 구성하려면 아래 처럼 설정
eureka.client.register-with-eureka: false

#Peer nodes 로부터 registry를 갱신할 수 없을 때 재시도 횟수
eureka.server.registry-sync-retrires (default: 5)

#Peer nodes 로부터 registry를 갱신할 수 없을때 재시도를 기다리는 시간
eureka.server.wait-time-in-ms-when-sync-empty (default: 3000) milliseconds

##Self-Preservation Mode(자가보존모드)
#Eureka로의 네트워크는 단절되었지만, 해당 서비스 API를 호출하는데 문제가 없는 경우가 있을수 있어서,
#self-preservation 을 사용하여 registry에서 문제된 instance를 정해진 기간 동안 제거하지 않을 수 있다.
eureka.server.eviction-interval-timer-in-ms (default: 60 * 1000)

#Post명령으로 수행해야함
endpoints:
  shutdown:
    enabled: true    # eureka에 종료를 알리고 서버 down: ip:port/shutdown
    sensitive: false
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
