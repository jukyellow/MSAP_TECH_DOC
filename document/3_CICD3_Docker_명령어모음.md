# Docker 명령어 모음

### 0. 도커설치
* window:
- 10홈 이하(docker toolbox on windows) : https://docs.docker.com/toolbox/overview/
- 10프로 이상(docker CE) 다운받아 설치

* 우분투 (전후 추가 작업이 있음..)
<code>
sudo apt-get update
sudo apt-get install docker-engine
</code>

* CentosOS (추가 업그레이드 필요 버그땜에..)
<code>
yum -y install docker (추가 업그레이드 필요 버그땜에..)
</code>

### 1. 도커 버전 확인
<code>
docker -v
</code>

### 2. root권한으로 docker image목록 확인
<code>
sudo docker images
</code>

### 3. 도커 컨네이너 확인
<code>
sudo docker ps -a
</code>

### 4. 도커에서 컨테이너(nginx 웹서버) 구동(없으면 다운받아 설치하고 구동됨)
> sudo docker run --rm --publish 8001:80 -it nginx
- 구글 클라우드에서는 방화벽규칙에서 8000을 먼저 열어야함
- 외부IP가 내가 접근가능한 주소임:  http://35.200.179.193:8000/


5)도커 ip 확인 (192.168.99.100)
>docker toolbox on windows에서는 IP가 실행시 찍혀서 보임
>sudo docker exec CONTAINER_ID ip addr show eth0
docker exec 11b9de80eecd ip addr show eth0


6)도커 컨네이트 실행종료
docker stop 컨테이너ID(5555b7dd1385)


7)도커 컨테이너 모두 삭제, 이미지삭제(rmi)
-컨테이너 하나 삭제
docker rm 컨테이너ID
-권한 없어서 전체삭제는 안됨?
docker rm $(docker ps -a -q)
-이미지 삭제
docker rmi [이미지명]


Exit 상태의 모든 컨테이너 삭제하기
docker rm $(docker ps --filter 'status=exited' -a -q)


*참고: Spring Boot로 웹app 개발시 내장 톰캣으로 동작하기 때문에 도커 톰캣이미지를 base이미지로 설치할필요 없음
         : Spring Boot jar/war파일은 java 명령어로 곧바로 수행가능
=> 8) 9) 11) 작업을 skip가능


8)도커 컨테이너 내부(shell) 접속하기 (디렉토리를 확인하고, 특정경로에 소스를 배포하기 위해)
--docker exec -it : STDIN 표준 입출력을 열고 가상 tty (pseudo-TTY) 를 통해 접속하겠다는 의미
docker exec -it  98a6916d8759 /bin/bash
*주의: 우분투 최신버전은 기본 /bin/sh(dash)를 사용해서 /bin/bash로 접속또는 Docker image생성시 문제가됨
>docker exec -it  98a6916d8759 /bin/sh


9)도커 컨테이너에 작업한 소스코드(호스트파일) 복사하기 -> 리빌드해야 data바뀜
-docker cp [host 파일경로] [container name]:[container 내부 경로]
-ex) nginx에 war파일 및 구동을 위한 설정변경파일 복사
docker cp testWebApp.war 11b9de80eecd:/usr/share/nginx/html
docker cp default.conf 11b9de80eecd:/etc/nginx/conf.d


10)도커 이미지 빌드(변경된 내용을 이미지로 배포후 구동해야 변경된 소스가 반영됨)
>Dockerfile 작성하여 도커파일이 있는 경로에서 빌드 수행(FROM,RUN,COPY,CMD,EXPOSE등등)
>도커>OS>웹서버>웹애플리케이션 배포
>도커명령어를 실행할 위치에 war, docker파일 복사해두기
--docker build -t [이미지명]  .
docker build -t nginx .


11)도커>톰캣 설치(서블릿으로 class/jsp를 구동하기 위함)
docker pull tomcat:8
docker run -d -i -t -p 8081:8080 tomcat:8
> 구동확인 : 192.168.99.100:8081
> war 배포 컨터이너안에 복사: docker cp testWebApp.war 9492a62f43d4:/usr/local/tomcat/webapps
> localhost:리슨포트>프로젝트명?war파일명? 실행확인: http://192.168.99.100:8081/testWebApp/
> 도커 이미지 빌드 : docker build -t tomcat2 .


12)도커 컨테이너(톰캣)실행
docker run -d -i -t -p 8081:8080 tomcat2:latest


13) 도커컨테이너<->호스트간 파일복사
13-1) 호스트->컨테이너 : docker cp [host 파일경로] [container name]:[container 내부 경로]
>ex: docker cp testWebApp.war 9492a62f43d4:/usr/local/tomcat/webapps
13-2) 컨테이너->호스트 : docker cp [container name]:[container 내부 경로] [host 파일경로]
>ex: docker cp 445a0ba19eea:/usr/local/tomcat/conf/server.xml c:/101_dimg
> 관련오류1: copying between containers is not supported (c:로 주는 경우 폴더 경로가 없어서 컨테이너로 인식)
> 관련오류2: open c:\server.xml: Access is denied. (c:/ root로 주는경우 권한없으므로 개별 폴더 지정필요)


14)도커 run 명령어 상세
>docker run -d -i -t -p 9001:9001 nginx_microsvc:latest
>docker run <옵션> <이미지 이름, ID> <명령> <매개 변수>
>-d: --detach=false: Detached 모드입니다. 보통 데몬 모드라고 부르며 컨테이너가 백그라운드로 실행됩니다.
>-i: --interactive=false: 표준 입력(stdin)을 활성화하며 컨테이너와 연결(attach)되어 있지 않더라도 표준 입력을 유지합니다.
     보통 이 옵션을 사용하여 Bash에 명령을 입력합니다
>-t: --tty=false: TTY 모드(pseudo-TTY)를 사용합니다. Bash를 사용하려면 이 옵션을 설정해야 합니다.
     이 옵션을 설정하지 않으면 명령을 입력할 수는 있지만 셸이 표시되지 않습니다.
>-p:  --publish=[]: 호스트에 연결된 컨테이너의 특정 포트를 외부에 노출합니다.
        보통 웹 서버의 포트를 노출할 때 주로 사용합니다.


15)dockerfile CMD 명령어 상세
>CMD ["nginx", "-g", "daemon off;"]
>CMD ["<실행 파일>", "<매개 변수1>", "<매개 변수2>"]
: 셸 없이 바로 실행할 때 매개 변수 설정하기
>daemon off : nginx.conf에 daemon off;로 설정했으므로 Nginx 웹 서버를 foreground로 실행합니다.
>-g: ? ground??


16)도커 소유권한 부여
>RUN chown -R www-data:www-data /var/lib/nginx
: root권한으로 {chown 소유권자:그룹식별자 바꾸고 싶은 폴더 이름}
: nginx파일? 경로를 www-data권한으로 지정 (root권한이 아니게 함->보안을 위해)


17) 도커 컨테이너 접속(stdout?)
>docker attach 도커ID


18) 도커 stdout 콘솔로그 trace 탈출(컨테이너 죽이지 않는방법)
>연속적으로 ctrl+p -> ctrl+q


19)실행 페라미터 전달
>docker run -e "SPRING_PROFILES_ACTIVE=dev" -p 8080:8080 -t springio/gs-spring-boot-docker


20)도커 port 확인
>docker port 도커ID
 : --net=host로 도커컨테이너 실행시, port정보가 확인되지 않는다(why?)
 : Eureka에 확인은 가능 or netstat - an명령어로 확인필요..


21)도커 로그 tail
>docker logs 컨테이너 ID
>docker logs -f --tail=5 1f72


22)로깅 관련
-log driver 설정 및 옵션 추가, 데몬구동
docker run --net=host --publish 8010:8010 --log-driver json-file --log-opt max-size=4m --log-opt max-file=5 -d -it msap-zuul-server echo msap-zuul-server start!
-서버의 docker로그 쌓이는 경로 확인
>docker inspect -f {{.LogPath}} a584bdba7334(도커 컨테이너 ID)
-docker log driver type 확인
>docker inspect -f '{{.HostConfig.LogConfig.Type}}' a584bdba7334(도커 컨테이너 ID)


-------------------------------------------------------------------------------------------------------


이미지 빌드/구동


0)도커파일
-----------------------------------------------------
FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE
COPY msap-zuul-server-0.1.0.war app.war
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.war"]
-----------------------------------------------------


1) 이미지 빌드(with dockerfile)
docker build -t msap-config-server .


2)실행
2-1) netword 모드
docker run --publish 9000:9000 -it msap-config-server (bridge 모드: 네트웍 격리)
or
docker run --net=host --publish 9000:9000 -it msap-config-server (host 모드: host와 IP공유)
2-2) 로깅(log-driver)
-log driver 설정 및 옵션 추가, 데몬구동
docker run --net=host --publish 8010:8010 --log-driver json-file --log-opt max-size=4m --log-opt max-file=5 -d -it msap-zuul-server echo msap-zuul-server start!


