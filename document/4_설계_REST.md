
# REST(Representational State Transfer)

[출처] Spring REST API 설계 및 구현 |작성자 가빈아빠 
http://blog.naver.com/PostView.nhn?blogId=koys007&logNo=220796287816&parentCategoryNo=&categoryNo=74&viewDate=&isShowPopularPosts=false&from=postView  

URI 형태
규칙 : 슬러시 구분자(/)는 계층관계를 나타내는 데 사용한다.  
규칙 : URI 마지막 문자로 슬래시(/)를 포함하지 않는다.  
규칙 : 하이픈(-)은 URI 가독성을 높이는 데 사용한다.  
규칙 : 밑줄(_)은 URI에 사용하지 않는다.  
규칙 : URI 경로는 소문자가 적합하다.  
규칙 : 파일 확장자(ex: .json, .xml)는 URI에 포함시키지 않는다.  

<hr />

Spring Framework + REST API 설계 및 구현  
>http://www.codingpedia.org/ama/tutorial-rest-api-design-and-implementation-in-java-with-jersey-and-spring/  

<hr />

https://www.slideshare.net/Byungwook/rest-api-60505484  

1)버전관리: api.서버명.com/서비스명/버전/리소스, ex)https://msapi.ktnet.com/msap/acps/v1/user/checkjoin   
2)API별 서버 URL이 다름->리버스 프록시로 해결가능.,.  

<hr />

http://mrrootable.tistory.com/75  

1)요청contents type제한
@RequestMappng(.., consumes="application/json")
