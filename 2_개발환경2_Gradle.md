# Gradle

1)개요

>구글이 gradle(그레이들)을 채택한이유
:https://brunch.co.kr/@yudong/67
>maven vs gradle 비교
:https://bkim.tistory.com/13

>gradle설명
:https://medium.com/@goinhacker/운영-자동화-1-빌드-자동화-by-gradle-7630c0993d09

>gradle 기본문법/기본파일 설명
 >기본 문법
 -task : ant의 target
 -repository/dependencies: maven 저장소와 참조라이브러리
 -compile: clourse(여기서는 함수객체?)로 선언 라이브러리를 컴파일시 사용
   >providedCompile : 프로젝트의 코드가 컴파일되는데 필요로 되는 의존성 라이브러리를 정의합니다. 
                             단 실제 런타임시에 컨테이너로부터 제공받기 때문에 빌드 결과물에 포함될 필요는 없는 라이브러리임을 뜻합니다. 
                             예로 Java ServletAPI나 JSTL 라이브러리가 있습니다.
   >testCompile : 테스트 실행시에 필요한 의존성을 정의합니다.
                        하지만 실제 프로젝트의 런타임에는 사용되지 않는 라이브러리임을 뜻합니다.
 >주요파일 설명
  -wrapper: gradle 바이너리를 자동으로 다운로드하여 별도의 gradle인스톨없이 사용가능
  -gradle.build: 빌드에 필요한 기본정보를 설정
  -settings.gradle : 멀티 프로젝트 구성시 사용(포함관계? 표현?)
 
>groovy 간단 문법: http://blog-joowon.tistory.com/2

--------------------------------------------------------------------------------------------------------------------

2) STS에서 gradle 사용법
 >gradle 프로젝트 생성시, STS에서는 gradle wrapper를 이용해 gradle 프로젝트를 구동할수 있게 해줌
 >gradle.build에 기본 빌드정보 작성 -> 프로젝트 우클릭 > gradle > Refresh Gradle Project하면 jar파일을 내려받음  

 >STS에서 gradle 빌드 및 jar/war 추출방법
  1)gradle tasks 탭확인(views 목록에 있음), run as에서는 안보임(wrapper방식으로 동작하기때문)
  2)bootWar실행> build/libs밑에 파일 생성됨

 >STS gradle 프로젝트 분석
 http://blog.naver.com/PostView.nhn?blogId=islove8587&logNo=220953725926&parentCategoryNo=&categoryNo=44&viewDate=&isShowPopularPosts=true&from=search

--------------------------------------------------------------------------------------------------------------------

3)gradle 멀티 프로젝트 구성
 >subprojects(하위 프로젝트 공통으로 적용 룰선언), project(개별하위 프로젝트 선언)
 >compile project(':module-common') : 공통모듈 참조 
 >공통모듈에서 main문이 없이 참조만 하게 할때,   bootRepackage {	enabled = false  } 추가선언 필요
   >스프링부트 2.0이상이면,  bootJar { enabled = false } jar { enabled = true }

 >case1: 멀티 프로젝트 심플예제
  :https://jojoldu.tistory.com/123
 >case2: STS(이클립스) 기반 멀티 프로젝트 구성예제(굿)
  :https://yookeun.github.io/java/2017/10/07/gradle-multi/
  :(github) https://github.com/yookeun/gradle-multi
 >프로젝트급? 예제(스프링부트 + gradle 프로젝트 예제 / 설명)
  http://gmind.tistory.com/entry/%EC%9E%91%EC%97%85%EC%A4%91-42-Gradle-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EB%A7%8C%EB%93%A4%EA%B8%B0-with-%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-%EB%A9%80%ED%8B%B0%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8%EA%B5%AC%EC%84%B1?category=655027

> 적용방법1) root 프로젝트에서 하위 프로젝트 모듈 참조정의하기(단점: 가독성은 좋겠으나 모듈이 증가할때 root를 수정해야함)
buildscript {}
subprojects {}
project(':module-api') {
    dependencies {
        compile project(':module-common')
    }
}
> 적용방법2) root에 정의하지 않고 하위프로젝트에 모듈참조를 정의(가독성은 조금 떨어지나 확장시 root수정없음)
apply plugin: 'war'
dependencies {
	compile project(':gradle-multi-core')
	compile('org.springframework.boot:spring-boot-starter-web') ...
}
--------------------------------------------------------------------------------------------------------------------
4) gradle test프로젝트 구문해석:

//gradle 빌드환경 설정
buildscript {
	ext { //프로젝트 전역에서 공유할 변수 선언
		springBootVersion = '2.1.1.RELEASE'
	}
	repositories { //maven 라이브러리 저장소와 동일
		mavenCentral()
	}
	dependencies { //레파지토리에서 아래 라이브러리를 다운받아 빌드함
		classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
	}
}

//해당모듈(플러그인)에서 제공하는 task와 task간 의존관계(best practice 라이브러리 모음)를 현재 프로젝트에 적용
apply plugin: 'java'
apply plugin: 'eclipse'
apply plugin: 'org.springframework.boot'
apply plugin: 'io.spring.dependency-management'
//apply plugin: 'war'

//war을 빌드해서 생성?
war {
    baseName = 'wartest'
    version = '0.0.1-SNAPSHOT'
}

//빌드정보
group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = 1.8

repositories {
	mavenCentral()
}

dependencies {
	implementation('org.springframework.boot:spring-boot-starter-web')
	testImplementation('org.springframework.boot:spring-boot-starter-test')
}

--------------------------------------------------------------------------------------------------------------------

5)Spring IO Platform 에서 제공하는 의존성관리 기능 설명

// 1. buildScript 선언
// Gradle에서 제공 되는 빌드 기능 이외의 직접 만든 Plugin 기능이나 외부 기능(외부 라이브러리)을 사용하고자 한다면 추가로 정의
buildscript {
    // dependencies.classpath의 외부 라이브러리가 있는 repositories를 별도로 선언
    repositories {
        jcenter()
    }
    // Spring IO Platform의 Gradle Plugin인 dependency-management-plugin 선언
    dependencies {
        classpath 'io.spring.gradle:dependency-management-plugin:0.5.1.RELEASE'
    }
}
 
// 2. plugin 적용
apply plugin: 'java'
// Spring Dependency Management Plugin을 사용
apply plugin: 'io.spring.dependency-management'
 
// 3. Spring IO Platform 버전 지정
// Gradle에는 원래 정의 되지 않은 task 이지만 io.spring.dependency-management plugin 적용으로 새롭게 생긴 task 이며 Spring IO Platform의 버전을 선언 하게 됩니다.
dependencyManagement {
    imports {
        // Spring Dependency Management의 버전을 지정 하여 관련 의존성 라이브러리 버전을 같이 관리함.
        mavenBom 'io.spring.platform:platform-bom:1.1.2.RELEASE'
    }
}
 
// 4. 의존성 라이브러리 설정
repositories {
    jcenter()
}
dependencies {
    // 버전을 기입 하지 않으면 Spring Dependency Management에서 관리 되고 있는 버전으로 자동으로 사용하게 되어짐.
    compile 'org.springframework.boot:spring-boot-starter-web'
}


> 출처: http://gmind.tistory.com/entry/4-Gradle-프로젝트-만들기-with-스프링부트?category=655027 [GMind]
--------------------------------------------------------------------------------------------------------------------

(삽질... 설치안해도 되는듯)
>window용 설치(생략가능?)
:https://zetawiki.com/wiki/%EC%9C%88%EB%8F%84%EC%9A%B0_gradle_%EC%84%A4%EC%B9%98
:https://gradle.org/install/
 >https://gradle.org/next-steps/?version=4.10.3&format=all

>이클립스(STS)에서 gradle 설치(업데이트) -> 불필요?
>help>marcket>buildship gradle integration 검색>설치(이미있어서 update로 실행)
 >STS에는 이미 설치되어있어서..하단 view에 gradle tasks에서 clean,build가능???

------------------------------------------------------------------------------------------------
