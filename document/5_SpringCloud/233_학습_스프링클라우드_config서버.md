#### -예제 참고:  https://yaboong.github.io/spring-cloud/2018/11/25/spring-cloud-config/


### 1.Config Client(개별 서비스) 설정


#### 1)conifg 서버의 properties 속성을 가져다 쓸때
<pre> `java
@RestController
@RefreshScope
public class ConfigClientController {
	@Value("${who.am.i}")
	private String identity;
	@GetMapping("/test")
	public String test() {
		return identity;
	}
}
</pre>


#### 2)개별 서비스에서 refresh로 다시 읽어올때
management:
	security:
		enabled: true
	endpoints:
	web:
		exposure:
			include: refresh # or '*'
- 개별서비스 port의 (actuator)refresh를 날려줌
- (POST) http://localhost:8081/refresh
- Refresh 정상동작여부 확인법 : 하단 3번 참고


#### 3)개별서비스에서 config 서버 설정
server:
	port: 8081

spring:
	application:
		name: yaboong
	cloud:
		config:
			uri: http://localhost:8080


#### 4)개별서비스의 dependency
dependencies {
	implementation('org.springframework.boot:spring-boot-starter-web')
	implementation('org.springframework.cloud:spring-cloud-starter-config')
	implementation('org.springframework.boot:spring-boot-starter-actuator')
}


#### 5)micro-service 실행시, 페라미터 전달( 환경변수로 spring.profiles.active 정보를 설정함)
>JVM옵션설정으로  Spring Profile 기능은 3.1 버전 이상부터 지원합니다.
-Dspring.profiles.active=live
-Dspring.profiles.active=dev
-Dspring.profiles.active=local
>local 스프링부트 실행시, bootstrap.xml에 spring.profiles.active={환경} 정보를 미리 세팅
>도커 실행시 Dockerfile ENDPOINT 부분에 설정(-Dspring.profiles.active=live)하고 시작?




### 2.Config server 설정


#### 1)git레파지토리에 YAML 파일생성
- ${ApplicationName}-${EnvironmentName}.yml 로 해주는 것이 디폴트
- ex) msap-zuul-server-dev.yml


#### 2)config 파일의 예
- msap-zuul-server-local.yml
<pre> `yml
#local enviroment
eureka:
  server:
    ip: localhost
    port: 8002    

zuul:
  server:
    port: 8010  #port: 8002 #8000~01:config, 8002~8009:Eureka, 8010~19:zuul, 8020~24:인증, 8025~29:Redis, 8100~:ms

#Zuul-Jdbc(nlps)
spring:
  db1:
    datasource:
      driverClassName : oracle.jdbc.driver.OracleDriver
      url : jdbc:oracle:thin:@000.000.000.0000:1521:cheetah
      username : nlps
      password : nlps2012
      maxActive : 5
#User-Info(KCSEDAS)
  db2:
    datasource:
      driverClassName : oracle.jdbc.driver.OracleDriver
      url : jdbc:oracle:thin:@000.000.000.0000:1521:coyote11
      username: kcsedas
      password: edaskcs_2016
      maxActive: 5

test:
  reload:
    val: reload data now 10!
</pre>

-yaboong-live.yml
who:
	am:
	i: live-yaboong

#### 3)Java Annotation 추가
<pre> `java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}
}
</pre>


#### 4) src/main/resources/application.yml 파일 : 레파지토리 연결
spring:
	cloud:
		config:
			server:
				git:
					uri: https://github.com/yaboong/spring-cloud-config-repository


#### 5)dependency 추가
dependencies {
	implementation('org.springframework.cloud:spring-cloud-config-server')
}
- ${ApplicationName}-${EnvironmentName}.yml 로 해주는 것이 디폴트


<hr />

### 3.Refresh 정상동작 확인방법

#### 1.  개별서비스에서 config정보 재로딩:  (POST) http://localhost:8010/refresh
![image](https://user-images.githubusercontent.com/45334819/60979652-0bba4e80-a36e-11e9-952c-6a44f6bdadbb.png)

#### 2. 로딩된 값 확인(RestController를 추가하여 변수값 찍는 코드 추가구현)
    => (GET) http://localhost:8010/config/reload
![image](https://user-images.githubusercontent.com/45334819/60979678-1543b680-a36e-11e9-8c5d-617d55221502.png)

#### 3. 새로운 값이 적용되지 않았으면, config서버에 로딩된 yml파일 확인
    => (GET) http://localhost:8010/msap-zuul-server/local
![image](https://user-images.githubusercontent.com/45334819/60979689-1a086a80-a36e-11e9-8c6f-24645650ebaf.png)

#### 4. config서버의 설정정보 강제 재로딩: (POST) http://localhost:8000/refresh
![image](https://user-images.githubusercontent.com/45334819/60979705-1ffe4b80-a36e-11e9-8f30-10a1471b51f1.png)
   
5.     1번을 다시 실행(개별서비스 refresh)하고 확인
