# MSAP (Micro Service API Platform) 개인 Project + 사내POC

> MSAP(Micro Serice API Platform) 프로젝트 개발에 사용된 주요기술요소 설명을 위한 레파지토리 입니다.  
> 요소기술에 대한 전반적인 이해보다, MSAP 개발에 사용된 기능위주의 설명으로 채워질 예정입니다.  
  
### 1. 1차 Proj  
 * 작업기간: 2018년 11월말 ~ 2019년 3월말 (4개월)  
 * 개발범위:   
   -백앤드(Spring Cloud): Eureka, Zuul, Config, Hystrix, Turbine등 간단하게 백앤드 구성  
   -사용자/어드민(부트스트랩, Vue): 사용자(Rest API 조회), 어드민(Spring Cloud Dashboar로 구성)   
   -배포환경: Docker(로컬 windows)  
 * 개발환경/개발언어: 아래 기술스택 참고  

(MSAP 구성도)  
![image](https://user-images.githubusercontent.com/45334819/54955639-33d59b80-4f91-11e9-9d63-db9609926fac.png)

(MSAP 기술스택)  
![image](https://user-images.githubusercontent.com/45334819/56145667-add0d180-5fdf-11e9-9bde-02316c7ca5ef.png)
  
(Eureka)  
![image](https://user-images.githubusercontent.com/45334819/54955752-8a42da00-4f91-11e9-8d20-554f359bd8b9.png)  

<hr>
  
### 2. 2차 Proj 고도화  
 * 작업기간(계획): 2019년 5월~ 2019년 6월(2개월)  
 * 작업범위 : 쿠버네티스/Knative 연동추가, Vue grid/DB연동/이벤트처리 추가  
  
<hr />

### 학습내용 정리:

#### 1. [MSA](https://github.com/jukyellow/msapdoc/blob/master/document/1_%EA%B0%9C%EC%9A%941_MSA.md "MSA")
#### 2. [Spring Cloud](https://github.com/jukyellow/Msap-Tech-Doc/blob/master/document/1_%EA%B0%9C%EC%9A%942_%EC%8A%A4%ED%94%84%EB%A7%81%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C.md "Spring Cloud")
  #### 2-0. [Eureka설정-HA구성](https://github.com/jukyellow/msa-side-proj/blob/master/document/5_%EA%B5%AC%ED%98%841_Backend4_Eureka.md)  
  #### 2-1. [Spring-Config-Server](https://github.com/jukyellow/msa-side-proj/blob/master/document/5_SpringCloud/233_%ED%95%99%EC%8A%B5_%EC%8A%A4%ED%94%84%EB%A7%81%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C_config%EC%84%9C%EB%B2%84.md "Spring-Config-Server")  
  #### 2-2. [Spring-Zuul-Server](https://github.com/jukyellow/msa-side-proj/blob/master/document/5_SpringCloud/232_%ED%95%99%EC%8A%B5_%EC%8A%A4%ED%94%84%EB%A7%81%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C_zuul%EC%84%9C%EB%B2%84(gateway).md)  
  #### 2-3. [Zuul-JDBC](https://github.com/jukyellow/msa-side-proj/blob/master/document/5_%EA%B5%AC%ED%98%841_BackEnd1_Zuul-JDBC.md)  
  #### 2-4. [Zuul-Ribbon](https://github.com/jukyellow/msa-side-proj/blob/master/document/5_%EA%B5%AC%ED%98%841_Backend2_Zuul-Ribbon.md)
  #### 2-5. [Zuul-Cookie-UUID](https://github.com/jukyellow/msa-side-proj/blob/master/document/5_%EA%B5%AC%ED%98%841_Backend3_Zuul-Cookie.md)  
  #### 2-6. [Zuul-Hystrix](https://github.com/jukyellow/msa-side-proj/blob/master/document/5_%EA%B5%AC%ED%98%841_Backend4_Zuul_Hystrix.md)  
  #### 2-7. [Nignx-Web Server](https://github.com/jukyellow/msa-side-proj/blob/master/document/5_%EA%B5%AC%ED%98%841_Backend8_Nginx%EC%84%9C%EB%B2%84_%EC%84%A4%EC%B9%98%EB%B0%8FPathch.md)  
  #### 2-8. [Zipkin,Sleuth](https://github.com/jukyellow/msa-side-proj/blob/master/document/5_%EA%B5%AC%ED%98%841_Backend5_Zipkin,Sleuth.md)
  #### 2-9. [무중단 배포](https://github.com/jukyellow/msa-side-proj/blob/master/document/5_%EA%B5%AC%ED%98%841_Backend7_%EB%AC%B4%EC%A4%91%EB%8B%A8%EB%B0%B0%ED%8F%AC.md)
  
  

### 개발환경

#### 1. [STS](https://github.com/jukyellow/Msap-Tech-Doc/blob/master/document/2_%EA%B0%9C%EB%B0%9C%ED%99%98%EA%B2%BD1_STS.md "STS")
#### 2. [Gradle](https://github.com/jukyellow/Msap-Tech-Doc/blob/master/document/2_%EA%B0%9C%EB%B0%9C%ED%99%98%EA%B2%BD2_Gradle.md "Gradle")
#### 3. [Logging(log4j2, logBack)](https://github.com/jukyellow/msa-side-proj/blob/master/document/2_%EA%B0%9C%EB%B0%9C%ED%99%98%EA%B2%BD7_%EB%A1%9C%EA%B9%85(Log4j2,LogBack,Slf4j).md)
#### 4. [Spring Boot + Mybatis](https://github.com/jukyellow/msa-side-proj/blob/master/document/2_%EA%B0%9C%EB%B0%9C%ED%99%98%EA%B2%BD4_Mybatis.md)


### Docker  

#### 1. [Docker](https://github.com/jukyellow/Msap-Tech-Doc/blob/master/document/3_CICD1_%EB%8F%84%EC%BB%A4.md "Docker")
#### 2. [Docker 명령어](https://github.com/jukyellow/msa-side-proj/blob/master/document/3_CICD3_Docker_%EB%AA%85%EB%A0%B9%EC%96%B4%EB%AA%A8%EC%9D%8C.md)
#### 3. [Docker DATA 관리(Logging,Volumn,Rotate,LocalTime)](https://github.com/jukyellow/msa-side-proj/blob/master/document/3_CICD4_Docker_Data%EA%B4%80%EB%A6%AC.md)  

### 운영환경

#### 1. [Docker 자원점유 관리](https://github.com/jukyellow/msa-side-proj/blob/master/document/6_%EC%9A%B4%EC%98%81%ED%99%98%EA%B2%BD1_Docker%EB%AA%A8%EB%8B%88%ED%84%B0%EB%A7%81.md)


### 설계

#### 1. [REST](https://github.com/jukyellow/Msap-Tech-Doc/blob/master/document/4_%EC%84%A4%EA%B3%84_REST.md "REST")

### Front End

#### 1. [Vue.js](https://github.com/jukyellow/Msap-Tech-Doc/blob/master/document/5_%EA%B5%AC%ED%98%842_UI1_Vue.js.md "Vue.js")
#### 2. [BootStrap](https://github.com/jukyellow/Msap-Tech-Doc/blob/master/document/5_%EA%B5%AC%ED%98%842_UI2_%EB%B6%80%ED%8A%B8%EC%8A%A4%ED%8A%B8%EB%9E%A9.md "BootStrap")


<hr />

### 참고 사이트

https://spring.io/projects/spring-boot  
https://kr.vuejs.org/  
http://bootstrapk.com/  
https://github.com/vuejs/awesome-vue#components--libraries  
https://github.com/sw300  
https://github.com/architectstory  
https://github.com/piomin  

<hr />

이 저작물은 [크리에이티브 커먼즈 저작자표시-비영리-동일조건변경허락 4.0 국제 라이선스](https://creativecommons.org/licenses/by-nc-sa/4.0/deed.ko/ "링크 제목 ") 국제 라이센스에 따라 이용할 수 있습니다.
