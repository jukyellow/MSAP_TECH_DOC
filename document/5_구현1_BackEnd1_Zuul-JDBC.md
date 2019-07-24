# Zuul-JDBC 
> Zuul서버의 Rounting 정보를 properties파일 대신 DB(Oracle) 테이블로 관리  

- #### Fork Project: https://github.com/jukyellow/zuul-route-oracle-spring-cloud-starter  

### 1. zuul-route-jdbc-spring-cloud-starter 분석
``` java
EnableZuulProxyStore클래스(annotaion으로 호출?)
->ZuulProxyStoreConfiguration 호출
> ZuulProxyAutoConfiguration 상속 (netflix)
> discoveryRouteLocator 재정의
->StoreRefreshableRouteLocator 호출 (netflix DiscoveryClientRouteLocator상속)
>supper : DiscoveryClientRouteLocator(netflix SimpleRouteLocator 상속)
   > supper: SimpleRouteLocator(netflix)
-> locateRoutes가 재정의되어 호출됨
>DiscoveryClientRouteLocator(netflix) locateRoutes가 선언되어있음
   : properties파일에서 읽어옴
=> properties속성도 읽고, 추가로 정의한 jdbc로딩도 add함
  > JdbcZuulRouteStore(imple ZuulRouteStore)
    > zuul.store.jdbc.enabled=true에 의해서 bean객체 등록됨
    > 테이블 조회 및 로딩
      : ZuulRouteRowMapper implements RowMapper
      : mapRow 재정의

* mapRow가 호출되는 순서분석:
유레카 시작?->ZuulProxyAutoConfiguration
->InstanceRegisteredEvent->reset->(zull 설정으로 제어가능할듯!)
>SimpleRouteLocator.doRefresh 170
>StoreRefreshableRouteLocator locateRoutes 34
>JdbcZuulRouteStore findAll 87
>RowMapperResultSetExtractor extractData 93
=> db조회시 결과를 결과를 zuul설정객체로 담아와서, 라우팅정보를 리턴
```

### 2. zuul-jdbc library Oracle Version 개발

#### 2-1. zuul-route-jdbc-spring-cloud-starter프로젝트 zuul관련 버전업(1.2.6->1.4.4) 및 deprecated모듈 수정
- zuul-route-jdbc-spring-cloud-starter-1.0.0.SNAPSHOT.jar -> zuul-route-jdbc-spring-cloud-starter-2.0.0.SNAPSHOT.jar 으로 업그레이드
- ZuulProxyStoreConfiguration.java :
 * ZuulProxyConfiguration -> ZuulProxyAutoConfiguration 변경
 * routeLocator -> discoveryRouteLocator 변경
 * @Autowired private ServiceInstance localServiceInstance 추가
- sample guide에 누락된 driver-class-name 추가

#### 2-2. msap-zuul-server 적용

##### 1) build.gradle dependencies에 외부jar 추가
``` groovy
compile files('libs/zuul-route-jdbc-spring-cloud-starter-2.0.0.SNAPSHOT.jar')
```

##### 2) gradle 외부 레파지토리 추가
``` groovu
maven{
    url 'https://nexus.ktnet.com/content/repositories/itps.release/' //nlps
}
```

##### 3) build.gradle dependencies에 oracle-db 연동관련 dependency 추가
- case1: dbcp2:2.6.0(jdk8)
- case2: dbcp1.3(jdk4~5)
``` groovy
implementation 'org.apache.commons:commons-dbcp2:2.6.0' //dbcp2 2.6.0->jdk8, dbcp 1.3->jdk4(nlps)
implementation 'org.springframework:spring-jdbc'  //JdbcTemplate, JdbcOperations
implementation 'nlps:ojdbc14_10:1.0' //oracle jdbc driver
```

##### 4) application.properties 파일추가 및 DB연결정보 추가
- 경로: /src/main/resources/ (소스파일에서 properties 속성정보를 사용함)
``` properties
spring.datasource.driverClassName=oracle.jdbc.driver.OracleDriver
spring.datasource.url=jdbc:oracle:thin:@xxx.xxx.xx.xx:1521:aaa
spring.datasource.username=aaa
spring.datasource.password=aaa
spring.datasource.maxActive=5
```

##### 5) zuul-jdbc에서 필요한 속성선언
- application.properties에 추가
``` properties
zuul.store.jdbc.enabled = true
```

##### 6) msap-zuul-server에 zuul-jdbc-library구동을 위한 @Bean객체 및 @EnableZuulProxyStore 추가
- @EnableZuulProxy -> @EnableZuulProxyStore 대체
- @EnableEurekaClient는 남아있어야함
```java
@SpringBootApplication
@EnableEurekaClient
@EnableZuulProxyStore //(2019.05.28,juk)load rout-info from Oracle-DB : @EnableZuulProxy->@EnableZuulProxyStore
public class MsapZuulServerApplication {

@Bean
public DataSource dataSource(
    @Value("${spring.datasource.driverClassName}") String driverClassName,
    @Value("${spring.datasource.url}") String url,
    @Value("${spring.datasource.username}") String username,
    @Value("${spring.datasource.password}") String password,
    @Value("${spring.datasource.maxActive}") int maxActive
) {
    
    System.out.println("url:"+url + ",driverClassName:"+driverClassName);
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName(driverClassName); //(2019.05.29,juk) add driver-class-name
    dataSource.setUrl(url);
    dataSource.setUsername(username);
    dataSource.setPassword(password);
    dataSource.setMaxActive(maxActive);
    
    return dataSource;
}

@Bean
public JdbcOperations mysqlJdbcTemplate(@Autowired DataSource dataSource) {
    System.out.println("mysqlJdbcTemplate called!");
    return new JdbcTemplate(dataSource);
}
```

##### 7) DB테이블 추가
- DDL(zuul_routes).sql 참고

##### 8) Eureka서버 구동후 Zuul서버 구동
- zuul-jdbc 모듈에서 @EnableDiscoveryClient 가 default로 적용됨

##### 9)주의 STRIP_PREFIX(1,0)에 대하여 ( Path경로 전달여부 결정)
- 1(true): path 전달제거 -> Reqest Mapping URL에서 path가 빠져야함
- 0(false): path 전달함 -> Request Mapping URL에  Path가 있어야함!
- 참고:  https://supawer0728.github.io/2018/03/11/Spring-Cloud-Zuul/
 

