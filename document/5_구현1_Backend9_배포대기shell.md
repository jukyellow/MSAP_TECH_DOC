# 서버 배포순서 유지위한 배포대기 쉘 스크립트

1. 파일 구성
: docker-compose => dockerfile(컨테이너 내부에 소스copy) => wait_for_server.sh(컨테이너 내부에서 대기 실행)

2. 동작설명
2-1. spring boot에 actuator 적용시, default {domain:port/health} rest-api 제공(http 200응답 수신)
2-2. 먼저 수행되어야할 서버 depends_on설정 및 해당서버 port connect
2-3. 선행서버 구동뒤 http 200응답을 받으면 대기서버 구동시작

3. 스크립트
- wait쉘
```
#!/bin/sh
# wait-for-server.sh
# https://docs.docker.com/compose/startup-order/
 
set -e
 
host="$1" #0.0.0.0
port="$2" #8001
 
shift 2
JAVA_OPTS="$@" #$JAVA_OPTS
>&2 echo "[host:port/uri] $host:$port/health";
>&2 echo "[JAVA_OPTS] $JAVA_OPTS";
 
if [ -n ${host} -a -n ${port} ];then
  while [ "$(curl -L -k -s -o /dev/null -w "%{http_code}\n" ${host}:${port}/health)" != "200" ]
  do
    >&2 echo "server wait(sleeping) > $host:$port/health ";
    sleep 2;
  done;
fi;
 
>&2 echo "server connected! > $host:$port"
exec java ${JAVA_OPTS} -jar ./app.war
```

- dockerfile
```
#FROM openjdk:8-jdk-alpine
FROM ubuntu:16.04

RUN apt-get update && \
    apt-get install -y openjdk-8-jdk && \
    apt-get install -y curl;
 
#RUN apt-get install -y curl
 
VOLUME /etc/timezone:/etc/timezone:ro
VOLUME /etc/localtime:/etc/localtime:ro
ENV TZ=Asia/Seoul
 
ARG APM_FILE
ARG APP_FILE
ARG SCOUTER_FILE
ARG SCOUTER_CONF
 
COPY ${APM_FILE} apm-agent.jar
COPY ${APP_FILE} app.war
COPY ${SCOUTER_FILE} scouter.agent.jar
COPY ${SCOUTER_CONF} scouter.conf
 
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
