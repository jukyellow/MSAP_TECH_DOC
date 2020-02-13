# Docker-compose 설정 및 Wait-for-server.sh 적용  

### docer-compose 명령어 정리
#### 0. 제약사항
 : docker-compose.yml파일이 존재하는곳에서 compose 명령어 사용가능  

#### 1.프로세스 목록 확인
$ docker-compose ps  

#### 2.컨테이너 이미지 생성(build)
2-1) yml에 선언된 컨테이너 하나만 빌드하는 경우  
$ docker-compose build {컨테이너명}  
2-2) 전체 빌드  
$ docker-compose build  
 
#### 3.컨테이너 종료&삭제(이미지, 볼륨)
3-1) yml에 선언된 전체 이미지를 삭제함  
$ docker-compose down --rmi -v  
•	--rmi: image 삭제  
•	-v: 볼륨 삭제  
3-2) yml에 선언된 한개 이미지 삭제  
$ docker-compose down {컨테이너명}  

#### 4.컨테이너 종료/실행
•	이미지 삭제없이, 컨테이너를 시작/종료만 수행  
4-1) yml파일 전체 start/stop  
$ docker-compose start  
$ docker-compose stop  
4-2) yml의 개별 컨테이너 start/stop  
$ docker-compose start {컨테이너명}  
$ docker-compose stop {컨테이너명}  

#### 5.컨테이너 네트워크 관리
5-1) 네트워크 목록확인  
$ docker  network ls  
5-2) 네트워크 정보 조회  
$ docker network inspect {network_id}  
5-3) 네트워크 삭제  
$ docker network rm {network_id}  
  
<br>


![image](https://user-images.githubusercontent.com/45334819/74351141-0fc5b780-4dfa-11ea-85cf-9907b3d17c5a.png)

![image](https://user-images.githubusercontent.com/45334819/74351152-148a6b80-4dfa-11ea-830e-c0ee6f2702bf.png)

- Dockerfile-ubuntu-openjdk
```#FROM openjdk:8-jdk-alpine
FROM ubuntu:16.04

RUN apt-get update && \
    apt-get install -y openjdk-8-jdk && \
    apt-get install -y curl && \
    apt-get install -y netcat;

VOLUME /etc/timezone:/etc/timezone:ro 
VOLUME /etc/localtime:/etc/localtime:ro 
ENV TZ=Asia/Seoul

ENV LC_ALL=C.UTF-8

ARG APM_FILE
ARG APP_FILE
ARG SCOUTER_FILE
ARG SCOUTER_CONF

RUN mkdir /conf

COPY ${APM_FILE} apm-agent.jar
COPY ${APP_FILE} app.war
COPY ${SCOUTER_FILE} scouter.agent.jar
COPY ${SCOUTER_CONF} /conf/scouter.conf

ARG ARG_JAVA_OPTS
ENV JAVA_OPTS=${ARG_JAVA_OPTS}

ARG ARG_WAIT_PORT
ENV WAIT_PORT=${ARG_WAIT_PORT}
ARG ARG_WAIT_HOST
ENV WAIT_HOST=${ARG_WAIT_HOST}

ARG WAIT_FILE
COPY ${WAIT_FILE} wait_for_server.sh
RUN chmod +x wait_for_server.sh

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
RUN export JAVA_HOME

#ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar ./app.war"]
ENTRYPOINT ["sh", "-c", "./wait_for_server.sh $WAIT_HOST $WAIT_PORT $JAVA_OPTS"]
```
