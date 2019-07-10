0.참고자료
1)자바기반의 마이크로서비스 이해와 아키텍처 구축하기
>https://github.com/architectstory/msa-architecture-config-server
2)마스터링 스프링클라우드
https://github.com/piomin/sample-spring-cloud-netflix/tree/config/sample-config-service

>config서버 설명: 
-개발/운영이 서로다른 공통변수(DB 접속정보등)를 설정파일로 등록해두고, ms가 구동될때 동적으로 참조가능하도록 함
-dev/real 환경정보를 git저장소에 한군데 저장하고, config서버로 로딩, 변경시 refresh로 반영등 효율적으로 환경정보 반영

>따라하기 좋은 설명 :  http://blog.leekyoungil.com/?p=352
 1) config 정보 xml 생성 :  {프로젝트명}-{환경명}.{yml 또는 properties}
   > 개발/운영이 서로다른 설정정보나, 서비스에서 공통적으로 쓰는 공통참조변수(상수)값들을 config에 기재
 2) config 서버 on : public/private 레파지토리 고려해서 git-url 설정 > 테스팅 : http://localhost:8888/msaconfig/greeting
 3) 사용(client)
  3-1) client 실행시 :  
    3-1-1) -Dspring.profiles.active={환경} 로 페라미터 세팅 > 해당 정보로딩하여 실행
    3-1-2) bootstrap.xml에 spring.profiles.active={환경} 정보를 미리 세팅
  3-2) bootstrap.yml파일은 spring cloud application에서 application.yml 파일보다 먼저 실행이 되기 때문에,
        config 서버의 설정을 써주면 bootrun 되기 전 Config Server에서 환경에 맞는 설정 파일을 불러와 실행이 되게 됩니다.
  3-3) 공통참조변수 사용방법 :  @RefreshScope추가 + refresh
    =>오류있어서 사용못함
    >@Value : 멤버변수에 적용  ex) @Value("${msapconfig.db-ip}") private String configDbIp;
    >@ConfigurationProperties : 클래스상단에 적용
    >https://m.mkexdev.net/414
  3-4) 동적 재반영 방법 : 
    3-4-1) config서버 변경파일(xml) 반영: http://~~:8888/config-server/refresh
    3-4-2) 개별 micro-service에 내려받기: ms재기동 or 도메인/ms주소/actuator/refresh 명령으로 config 재로딩 가능

 4)참고소스
 >https://github.com/yaboong/spring-cloud-config-server
 >https://github.com/yaboong/spring-cloud-config-client/blob/master/src/main/resources/application.yml
----------------------------------------------------------------------------------------------

1. dependency

1-1) 버전(마스터링 스프링클라우드)
-<java.version>1.8</java.version>
-springBootVersion = '1.5.6.RELEASE'
-springCloudVersion = 'Dalston.SR4'

1-2) 라이브러리 (자바기반 마이크로.. 구축하기)
compile('org.springframework.boot:spring-boot-starter-actuator')
	compile('org.springframework.cloud:spring-cloud-config-server')
	testCompile('org.springframework.boot:spring-boot-starter-test')

2. config & annotaion

2-1) yml 
 -----------------------------------------------------------
>config서버 application.yml
server:
  port: 8888
  
spring:
  application:
    name: msa-architecture-config-server
    
  cloud:
    config:
      server:
        git: 
          uri: https://github.com/architectstory/msa.git
          username: username
          password: password

>git저장소 config-server.yml
msaconfig:
  greeting: "hello"
  
---

spring:
  profiles: local

msaconfig:
  greeting: "welcome to local server"

---

spring:
  profiles: dev

msaconfig:
  greeting: "welcome to dev server"

---

 -----------------------------------------------------------

2-2) java 
-서버구동: @EnableConfigServer
-추가기능:
 >설정재반영: http://주소/프로파일명(git설정파일명)/refresh
   ex) http://~~:8888/config-server/refresh
 >config정보 읽기(get) ex) http://localhost:8888/msaconfig/greeting

