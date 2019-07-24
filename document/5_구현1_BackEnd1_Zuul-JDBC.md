# Zuul-JDBC 
> Zuul서버의 Rounting 정보를 properties파일 대신 DB(Oracle) 테이블로 관리  
> Fork Project: https://github.com/jukyellow/zuul-route-oracle-spring-cloud-starter  

- zuul-route-jdbc-spring-cloud-starter 분석
<pre>
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
</pre>
 

