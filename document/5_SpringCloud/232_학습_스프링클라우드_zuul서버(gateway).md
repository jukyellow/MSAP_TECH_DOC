0.참고자료
1)자바기반의 마이크로서비스 이해와 아키텍처 구축하기
>https://github.com/architectstory/msa-architecture-zuul-server
2)마스터링 스프링클라우드
>https://github.com/piomin/sample-spring-cloud-netflix/tree/cluster/sample-gateway-service

3)활용/이론 참고
>https://elky84.github.io/2018/09/23/java_zuul_use_story/

>zuul서버 설명: 
-사용자요청이 들어오면 해당 마이크로서비스를 라우팅정보에 따라 라우팅하는데,
 이때 서비스ID를 유레카서버에서 찾아서 라우팅 경로를 설정하게 됨.

>zuul필터, 라우팅 동적변경
https://nevercaution.github.io/2018/10/23/api-gateway-with-zuul/
----------------------------------------------------------------------------------------------

1. dependency

1-1)버전 (마스터링 스프링클라우드)
-<java.version>1.8</java.version>
-springBootVersion = '1.5.6.RELEASE'
-springCloudVersion = 'Dalston.SR4'

1-2)라이브러리(자바기반 마이크로.. 구축하기)
	compile('org.springframework.boot:spring-boot-starter-actuator')
	compile('org.springframework.cloud:spring-cloud-starter-config')
	compile('org.springframework.cloud:spring-cloud-starter-eureka')
	compile('org.springframework.cloud:spring-cloud-starter-feign')
	compile('org.springframework.cloud:spring-cloud-starter-ribbon')
	compile('org.springframework.cloud:spring-cloud-starter-zuul')	
	testCompile('org.springframework.boot:spring-boot-starter-test')


2. config & annotaion

2-1) yml (자바기반 마이크로.. 구축하기)
 -----------------------------------------------------------
>zuul서버 application.yml
server:
  port: 9090
    
spring:
  application:
    name: msa-architecture-zuul-server

#Config Server      
  cloud:
    config:
      uri: http://localhost:8888 
      name: msa-architecture-config-server

#Eureka Client   
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:9091/eureka/
      
#Zuul Routing    
zuul:
  routes:
    coffeeOrder: #routing id
      path: /coffeeOrder/** #zuul context root
      serviceId: msa-service-coffee-order #spring application name
      
    coffeeMember:
      path: /coffeeMember/** 
      serviceId: msa-service-coffee-member  
      
    coffeeStatus: 
      path: /coffeeStatus/**
      serviceId: msa-service-coffee-status

>클러스터 3중화 구성예제 : https://github.com/piomin/sample-spring-cloud-netflix/blob/cluster/sample-gateway-service/src/main/resources/application.yml
 -----------------------------------------------------------

2-2) java 
-서버구동: @EnableZuulProxy, @EnableEurekaClient
-추가기능 (자바기반 마이크로.. 구축하기)
 >필터재정의 
   1) HttpServletRequest/Response 잡아서 변경
      ex)public class SimpleFilter extends ZuulFilter { ... }
   2) 라우팅정보는 기본 설정파일에서 로딩하도록 되어있는데, DB에서 로딩하도록 재정의가 가능하다고함...
