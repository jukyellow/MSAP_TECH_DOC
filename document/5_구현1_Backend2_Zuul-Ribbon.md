# Zuul 로드밸런싱 설정(By Ribbon) 및 재시도설정

### 1.dependencies 추가

- 참고: https://jsonobject.tistory.com/464  
- 설정:  
```
dependencies {
    implementation('org.springframework.cloud:spring-cloud-starter-netflix-ribbon')
}
```
>spring-cloud-starter-netflix-zuul 아티팩트는 zuul-core 1.3.1(2017-11-01 출시) 버전에 의존적이다.
>관리 주체인 Spring Cloud 팀은 향후 zuul-core 2 버전을 지원하지 않을 예정으로 해당 모듈을 자사의 spring-cloud-gateway로 변경할 것을 권장하고 있다.
>아마 Spring Cloud 팀이 Netflix Zuul을 경쟁자로 보기 시작하면서 자체적인 생태계를 구축하려는 시도로 보인다.
>한편, 분산 스로틀링을 기본 기능을 제공하지 않는데 필요한 경우 써드파티 모듈인 spring-cloud-zuul-ratelimit 아티팩트를 추가하면 된다. [관련 링크]
>#zuul.routes.url을 직접적으로 명시하면 Netflix Ribbon을 사용하지 않는다.

----------------------------------------------------------------------

### 2. Zuul Ribbon 라우팅 설정

- 참고: https://gunju-ko.github.io/spring-cloud/netflixoss/2018/12/14/Ribbon.html
```
# 쓰레드 격리 전략 설정
zuul:
  riboon-iolation-stategy: thread
  thread-pool:
    use-separate-thread-pools: true

#Client Configuration Options
#<clientName>.<nameSpace>.<propertyName>=<value>
#프로퍼티에 clientName이 없으면, 모든 클라이언트에 적용되는 프로퍼티가 된다.

#Caching of Ribbon Configuration

ribbon:
  eager-load:
    enabled: true #기본 lazy로드(해당 client의 첫 요청이 들어왔을때 로딩됨)대신 eagerly 로드
    #clients: airCargoTrace.do, airCargoTrace, airCargoTrace2, air-cargo-trace, air-cargo-trace2
  #ConfigurationBasedServerList로부터 정보를 받아옴...
  NIWSServerListClassName: com.netflix.loadbalancer.ConfigurationBasedServerList
  #유레카 클라이언트로부터 서버목록을 가져온다
  #NIWSServerListClassName: com.netflix.niws.loadbalancer.DiscoveryEnabledNIWSServerList
  #eureka:
  #  enabled: true
  # Timeout 설정값은 Hystrix의 값과 비교하여 작은 값이 우선 적용 된다.
  ConnectTimeout: 1000  # Connect timeout used by Apache HttpClient              
  ReadTimeout: 2000     # Read timeout used by Apache HttpClient
  MaxTotalHttpConnections: 500 # 서버의 전체 갯수?
  MaxConnectionsPerHost: 10    # client당 동시접속 갯수?
  #retryableStatusCodes: 503
  MaxAutoRetries: 10              # Max number of retries on the same server (excluding the first try)
  MaxAutoRetriesNextServer: 10    # Max number of next servers to retry (excluding the first server)
  OkToRetryOnAllOperations: true  # Whether all operations can be retried for this client
  ServerListRefreshInterval: 3000 # Interval to refresh the server list from the source
#  feign:
#    hystrix:
#      enabled: false

#다른 Zone의 Client는 호출에서 제외할때
#EnableZoneAffiniy: true
#전체 서버중 고정개수의 서버만 호출할때
#ServerListSubsetFilter:
#  size: 5
```

--------------------------------------------------------------------------------------------------------------------

-기타 shutdown(post) 명령어  
```
#Post명령으로 수행해야함
endpoints:
  shutdown:
    enabled: true    # eureka에 종료를 알리고 서버 down: ip:port/shutdown
    sensitive: false
```
