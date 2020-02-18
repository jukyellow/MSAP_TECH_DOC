# Docker Private Registry

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
