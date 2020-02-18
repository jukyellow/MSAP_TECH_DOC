# Docker Private Registry와 Base 이미지 관리

### 1. Private Registry 사용법

```
1)태그명 naming 룰 명명
  IP:PORT/alpine:3.10.3
2)태그명 docker-hub 이미지 다운받고->tag명 복제
  docker tag alpine:3.10.3 IP:PORT/alpine:3.10.3
3)docker-hub image삭제
  docker rmi -f alpine:3.10.3
4)로컬 레지스트리 업로드
  docker push IP:PORT/alpine:3.10.3
(다운로드 확인)
 docker pull IP:PORT/alpine:3.10.3
 ```
 
 ### 2. Private Registry 설치
 
 <br>
 <hr>
 
 ### 3. Base Image 관리  
- base 이미지 관리 및 사이즈 최소화  
1)application base image: Ubuntu, OpenJDK  
2)경량화 버전: ububtu->alpine(openjdk8->openjdk8-jre), openjdk:8-jre-alpine   
  > 최종 application 이미지 사이즈: 564M → 144M ,  156M → 136M로 줄임  
  
- Alpine 3.10.3  
>스펙: ubunut 16.04(linux kernel:4.4), apline 3.10.3 (linux kernel:4.19.53)  
>명령어: apt-get install 대신, apk add 사용  
>패키지 :ubuntu jdk package 'openjdk-8-jre' -> 'openjdk8-jre'  
>사이즈: Ubuntu (124M) → Alpine(5.5M)  

- Openjdk:8-jre-alpine  
>스펙: Docker hub Official(도커허브인증), Openjdk 경량화 버전(alpine)  
>사이즈: openjdk:8(105M→85M)  
