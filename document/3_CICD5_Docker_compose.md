# Docker-compose 설정 및 Wait-for-server.sh 적용  

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
