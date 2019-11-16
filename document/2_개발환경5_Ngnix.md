# Ngnix

### 잘정리된 사이트:
1. 개요 : https://opentutorials.org/module/384/4530  
   상세설명: http://whatisthenext.tistory.com/123  
2. location 개로: http://kwonnam.pe.kr/wiki/nginx/location  
3. location 정규식: http://ohgyun.com/480  
4. 설정별 간결설명 + proxy_params: http://areumgury.blogspot.com/2016/08/nginx.html  
5. location proxy pass의 '/'여부에 따른 URI전달 차이점 설명(중요): http://ohgyun.com/621  

---

### 1. 디렉토리 구조(도커 nginx이미지), 환경설정 파일  
- nginx.conf :  애플리케이션의 기본 환경 설정  
- root/conf.d/default.conf : server관련 설정모음  
- root/sites-enabled/default : 활성화된 사이트 설정모음?  
- root/sites-available/default : 비활성화된 사이트 설정모음?  
- proxy_params : proxy 헤더 전달 페리미터 설정  
  
### 2. 주요설정 및 설명  
- nginx.conf  
```
user nginx	  #default www-data, 워커 프로세스(웹서버역할)의 권한 지정  
worker_processes  1;  #워커 프로세스의 수, 코어 갯수만큼 설정하면 됨  
events { 		 #비동기 이벤트 처리옵션  
    worker_connections  1024;	         #한번에 처리할수 있는 커넥션 수   
    #multi_accept on; (디폴트값 : off) #  
}  
http{	  #server,location 블록을 묶음, 선언된 설정을 하위 블록이 상속받음  
     server{	  #가상호스트(웹app서비스를 제공하는 하나의 도메인)  
          listen 80;                              #리슨 포트  
          server_name exam.com;          #도메인명  
          access_log /var/log/nginx/exam.com.log;  #접속로그  
          location / {	#특정 URL을 처리하는 방법( / : root요청인 경우)  
            root   html;         	           #정적파일 경로설정(URI를 제외한 서버경로)  
            index  index.html index.htm;   #기본 로딩페이지 찾는 순서?  
            // include proxy_params;        #가상호스트(웹app서비스)로 전달할 설정들  
          }  
          location ~ \.do$ {	  #location 정규식으로 특정 URL(~.do로 끝나면) 처리  
              proxy_pass  http://localhost:8080;   #설정 URL로 리다이렉트?  
          }  
          location ~ \.jsp$ {	     #location 정규식으로 특정 URL(~.jsp로 끝나면) 처리  
            #cgi: 웹서버가 처리할수 없을때 외부에서 처리한 결과를 받아서 client로 전송  
            #fastcgi: 요청시마다 프로세스를 생성하지 않고, 생성해둔 프로세스에서 빠르게 요청처리  
            fastcgi_pass   was_server;    #웹app서비스를 제공하는 WAS서버 및 호출시 설정정보  
            fastcgi_index  index.jsp;     #WAS서버 초기 페이지  
            fastcgi_param  SCRIPT_FILENAME  $document_root$fastcgi_script_name; #?  
            include        fastcgi_params;  #WAS서버로 전달할 페라미터 설정파일     
         }  
     }  
    #웹서버->WAS서버로 호출시 설정정보 세팅  
    #'server'가 멀티가 존재하면 라운드로빈 방식으로 호출   
    upstream was_server{     
        ip_hash;	#sticky session 설정(client접속이 하나의 WAS로만 호출하여 처리, 로그인세션유지등을 위해) 
        server 192.168.125.142:9000 weight=3; #weight설정: 다른서버보다 3배많이 호출  
        server 192.168.125.143:9000;  
        server 192.168.125.144:9000 max_fails=5 fail_timeout=30s; #30초간 응답이 없고 5번반복시 호출중지  
        server unix:/var/run/php5-fpm.sock backup;  평상시엔 사용안하다가, 모든서버가 불능일때 사용  
    }  
}  
```

- Location 변경자 정규식 문법:  

> location [=|~|~*|^~|@] pattern { … }  
```  
= : 지정 패턴과 정확히 일치  
     예)  
     server {  
          server_name website.com;  
          location = /abcd {  
          }  
     }  
     http://website.com/abcd (일치)  
     http://webstie.com/ABCD (대소문자 구분은 운영체제에 따른다)  
     http://website.com/abcd?param1&param2 (일치. 질의 문자열은 패턴 매칭과 상관없다)  
     http://website.com/abcd/ (불일치)    
  
‘’ : 지정한 패턴으로 시작해야 한다. ??  
     http://website.com/abcd/ (일치)  
     http://website.com/abcde (일치)  
~ : 정규표현식과 일치  
~* : 대소문자를 구분하지 않으며 정규표현식과 일치  
^~ : 지정한 패턴으로 시작해야 한다. 패턴이 일치하면 엔진엑스가 다른 패턴의 탐색을 중지한다.    
     (정규표현식이 아닌 것에 주의한다)  
@ : 이름가진 location 블럭을 정의한다. 내부 요청에 의해서만 접근할 수 있다.  
```

### 3. 검색 순서와 우선순위  
우선순위가 높은 것과 매칭한다.   
블럭을 정의한 순서와는 관계가 없다.  
  
우선순위  
요청 URI가,  
1. = 변경자를 갖는 location 블럭의 문자열과 일치  
2. 변경자가 없는 location 블럭의 문자열과 일치  
3. ^~ 변경자를 갖는 location 블럭의 시작 부분과 일치  
4. ~ 또는 ~* 변경자를 갖는 location 블럭의 패턴과 일치  
5. 변경자가 없는 location 블럭의 시작부분과 일치  
  
- 사례
```
server {  
  server_name website.com;  
  location /doc { (A) }  
  location ~* ^/document$ { (B) }  
}  
http://website.com/document를 요청하면, (B)가 매칭  
  
server {  
  server_name website.com;  
  location /document { (A) }  
  location ~* ^/document$ { (B) }  
}  
http://website.com/document를 요청하면, (A)가 매칭   
 
server {  
  server_name website.com;  
  location ^~ /doc { (A) }  
  location ~* ^/document$ { (B) }  
}
```
http://website.com/document를 요청하면, (A)가 매칭   
  
--- 
  
- Location proxy_pass에 '/'에 따른 URI전달 차이점 설명(http://ohgyun.com/621):  
```   
Case1) location 과 proxy_pass 에 / 가 없는 경우  
     location ^~ /foo {  
         proxy_pass http://localhost:3000;  
     }  
     --> 프록시로 서버로 전달되는 path: /foo/bar/baz  
Case2) location 에 / 가 있고, proxy_pass 에 / 가 없는 경우  
     location ^~ /foo/ {  
         proxy_pass http://localhost:3000;  
     }  
     --> 프록시로 서버로 전달되는 path: /foo/bar/baz  
Case3) location 에 / 가 없고, proxy_pass 에 / 가 있는 경우  
     location ^~ /foo {  
         proxy_pass http://localhost:3000/;  
     }  
     --> 프록시로 서버로 전달되는 path: //bar/baz  
           location 블럭에서 매칭된 나머지 주소(/bar/baz)가 프록시 주소의 마지막에 붙어 전달된다.  
*case3처럼 짤리는 경우가 있으므로 주의해야함, 대략 10가지 정도의 case가 있는듯...  
```

---
  
#### 기타 상세설정, 키워드 설명: 
```
#proxy_param 설정 : 웹서버->WAS 호출시 전달할 http 페라미터 전달  
#미설정시 default 상속되어 전달, 하나라더 설정시 나머지는 모두 skip됨으로 주의  
proxy_set_header X-Real-IP $remote_addr;  
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;  
proxy_set_header Host $http_host;  
proxy_set_header X-NginX-Proxy true;  
  
client_max_body_size 100M;  
client_body_buffer_size 1m;  
proxy_intercept_errors on;  
proxy_buffering on;  
proxy_buffer_size 128k;  
proxy_buffers 256 16k;  
proxy_busy_buffers_size 256k;  
proxy_temp_file_write_size 256k;  
proxy_max_temp_file_size 0;  
proxy_read_timeout 300;  
proxy_redirect off;  
```

#### NginX error_log 파일 추가  
- default.conf 파일에 error_log path 추가  
- 참고 : https://medium.com/sjk5766/docker-compose%EB%A1%9C-localhost-nginx-%EB%A6%AC%EB%B2%84%EC%8A%A4-%ED%94%84%EB%A1%9D%EC%8B%9C-%EA%B5%AC%EC%84%B1-8214d41a94fc  





