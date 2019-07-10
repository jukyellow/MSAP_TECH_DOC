# Gradle

-Gradle의 Maven보다 좋은점
1. 빌드와 테스트 실행 결과 Gradle이 더 빠르다. (Gradle이 캐시를 사용하기 때문에 테스트 반복 시 차이가 더 커진다.) > Maven보다 100배 빠름
2. 스크립트 길이와 가독성 면에서 Gradle(groovy)이  앞선다.
3. 의존성이 늘어날 수록 성능과 스크립트 품질의 차이가 심해질 것이다
4.  Build라는 동적인 요소를 XML로 정의하기에는 어려운 부분이 많다.
             Gradle은 Groovy를 사용하기 때문에, 동적인 빌드는 Groovy 스크립트로 플러그인을 호출하거나 직접 코드를 짜면 된다.


-참고:  https://bkim.tistory.com/13
Ant와 Maven의 장점을 모아모아 2012년 출시
Android OS의 빌드 도구로 채택 됨


Gradle이란 무엇인가?
* Ant처럼 유연한 범용 빌드 도구 (A very flexible general purpose build tool like Ant.)
* Maven을 사용할 수 있는 변환 가능 컨벤션 프레임 워크 (Switchable, build-by-convention frameworks a la Maven. But we never lock you in!)
* 멀티 프로젝트에 사용하기 좋음 (Very powerful support for multi-project builds.)
* Apache Ivy에 기반한 강력한 의존성 관리 (Very powerful dependency management (based on Apache Ivy))
* Maven과 Ivy 레파지토리 완전 지원 (Full support for your existing Maven or Ivy repository infrastructure.)
* 원격 저장소나, pom, ivy 파일 없이 연결되는 의존성 관리 지원
(Support for transitive dependency management without the need for remote repositories or pom.xml and ivy.xml files.)
* 그루비 문법 사용 (Groovy build scripts.)
* 빌드를 설명하는 풍부한 도메인 모델 (A rich domain model for describing your build.)
 




[STS에서 gradle 사용법] 
1. UPDATE
![image](https://user-images.githubusercontent.com/45334819/60978610-5b981600-a36c-11e9-8c35-bfa148703221.png)

2.clean, Build
![image](https://user-images.githubusercontent.com/45334819/60978453-22f83c80-a36c-11e9-876a-dcdaed175764.png)

3. gradle.build..
![image](https://user-images.githubusercontent.com/45334819/60978475-2d1a3b00-a36c-11e9-8ac5-aac93496ed71.png)



[기본 문법]


>기본 문법
-repository/dependencies: maven 저장소와 참조라이브러리
-compile: clourse(여기서는 함수객체?)로 선언 라이브러리를 컴파일시 사용
   >providedCompile : 프로젝트의 코드가 컴파일되는데 필요로 되는 의존성 라이브러리를 정의합니다.
                             단 실제 런타임시에 컨테이너로부터 제공받기 때문에 빌드 결과물에 포함될 필요는 없는 라이브러리임을 뜻합니다.
                             예로 Java ServletAPI나 JSTL 라이브러리가 있습니다.
   >testCompile : 테스트 실행시에 필요한 의존성을 정의합니다.
                        하지만 실제 프로젝트의 런타임에는 사용되지 않는 라이브러리임을 뜻합니다.
-implementation : 이미 존재하는 라이브러리는 제외하고 의존성 라이브러리를 로딩
>주요파일 설명
  -gradle.build: 빌드에 필요한 기본정보를 설정
  -settings.gradle : 멀티 프로젝트 구성시 사용(상속관계 표현)


>STS gradle 프로젝트 분석
http://blog.naver.com/PostView.nhn?blogId=islove8587&logNo=220953725926&parentCategoryNo=&categoryNo=44&viewDate=&isShowPopularPosts=true&from=search
