# Docker Data 관리

## [ 2.docker 컨테이너 로그 서버저장(Volumn 설정) ]

### 1. 도커 데이터 관리 기본
* 참고 : https://medium.com/dtevangelist/docker-%EA%B8%B0%EB%B3%B8-5-8-volume%EC%9D%84-%ED%99%9C%EC%9A%A9%ED%95%9C-data-%EA%B4%80%EB%A6%AC-9a9ac1db978c

#### * 도커 Volumn/Bind Mount 예제
- 도커 Volume mount 개념설명: http://wiki.rockplace.co.kr/pages/viewpage.action?pageId=3868698  
- 도커 Volume 설정하는 다양한 방법: https://darkrasid.github.io/docker/container/volume/2017/05/10/docker-volumes.html  

##### 1) 기본설정(경로를 hash코드로 자동생성하는 경우)  
- 명령어 예제
``` sh
#docker 컨테이너 내부 로그경로(/logs) -> 서버 volumn경로로 매핑
docker run --net=host --publish 8010:8010 --log-driver json-file --log-opt max-size=4m --log-opt max-file=10 -d -it --name msap-zuul-server -v /logs docker.io/msap-zuul-server

docker inspect msap-zuul-server

#저장경로 확인
docker inspect --format="{{.Mounts}}" msap-zuul-server

# 로그저장 경로 ex)
# 기본 was 로그와 동일 daily, sql, .out파일로 구성
/var/lib/docker/volumes/2dee171d2c98769b932fb1ab986f5d4442e60f1bf54e11db9357a78e68cdae10/_data/daily

#도커 삭제시 볼륨도 삭제
>docker rm -v <container id or name>
-참고:  https://riptutorial.com/ko/docker/example/10567/%EB%8F%84%EC%BB%A4-%EB%B3%BC%EB%A5%A8-%EC%A0%9C%EA%B1%B0--%EC%82%AD%EC%A0%9C-%EB%B0%8F-%EC%A0%95%EB%A6%AC
```

* Dockerfile (볼륨지정하여 실행명령어에서 생략)
``` yml
VOLUME /logs
```
##### 2) Volume name을 설정하는 경우  
- 볼륨이름생성 -> 실행명령어 설정   
``` sh
docker volume create --name msap-zuul-server-logs
docker run --name air-cargo-trace-test --net=host -d -p 8909:8909 -v msap-zuul-server-logs:/logs  msap-zuul-server
```
- dangling 볼륨 삭제: docker volume rm $(docker volume ls -qf dangling=true)

##### 3) 컨테이너간 볼륨 경로 공유
- 참고:  https://jungwoon.github.io/docker/2019/01/13/Docker-3/  
- 주의사항: 기존 컨테이너의 볼륨을 공유함으로 컨테이너가 떠있어야 실행가능(기존 컨테이너가 죽으면 실행불가)  
```
컨테이너 생성시 --volumes-from 옵션을 사용하면 -v 옵션이 적용된 컨테이너의 볼륨 디렉토리를 공유할 수 있습니다.
$ docker run -i -t \
> --name volumes_from_container \ # --name : 컨테이너 이름
> --volumes-from wordpressdb_hostvolume \ # --volumes-from [컨테이너 이름] : -v가 설정된 컨테이너의 볼륨을 같이 공유
> wordpress # 이미지 이름
```

<br>

## [ 3.docker 컨테이너 TimeZone(현재시간) 설정 ]

#### [ Dockerfile방식 ]

* dockerfile 방식 참고:  https://devmas.tistory.com/entry/Docker-Container%EC%9D%98-Timezone%EC%9D%84-Host-OS%EC%99%80-%EB%A7%9E%EC%B6%94%EA%B8%B0

- Dockerfile
``` sh
VOLUME /etc/timezone:/etc/timezone:ro
VOLUME /etc/localtime:/etc/localtime:ro
ENV TZ=Asia/Seoul
```
- /lib/var/docker/volumn/컨테이너별uniq경로/_data/로그파일들
<br>

### [ 4-1.Rotate(파일 백업 설정) ]
- 참고:  http://egloos.zum.com/mcchae/v/11259352

``` sh
$ sudo vi /etc/logrotate.d/docker-container
/var/lib/docker/containers/*/*.log {
  rotate 7
  daily
  compress
  size=1M
  missingok
  delaycompress
  copytruncate
}
```

rotate 7 : 최대 log.1, log.2 등 7개의 파일을 보관하고
daily : 날마다 rotate 시키며
compress : 이전 로그는 압축하며
size=1M : 크기가 1메가 바이트를 넘으면 로테이트,
missingok : 해당 로그가 없어도 ok,
delaycompress :
copytruncate : 복사본을 만들고 크기를 0으로

- 현재 로그를 우선 적용
$ sudo logrotate -fv /etc/logrotate.d/docker-container
> 백업된 파일 확인


### [ 4-2.log-driver 설정 ]

#### 1.서버 Docker로그(STDOUT) 저장경로
> /var/lib/docker/container/...(도커container별)/...(.json파일, unicode포함)
- http://blog.naver.com/PostView.nhn?blogId=pjt3591oo&logNo=221404622219&redirect=Dlog&widgetTypeCall=true&directAccess=false

#### 2.도커 log-driver 설정 및 구동
- log driver 설정 및 옵션 추가, 데몬구동
docker run --net=host --publish 8010:8010 --log-driver json-file --log-opt max-size=4m --log-opt max-file=5 -d -it msap-zuul-server echo msap-zuul-server start!

- https://docs.docker.com/config/containers/logging/configure/
- https://docs.docker.com/config/containers/logging/json-file/

#### 3.log path확인 및 권한설정
- 도커 재기동시 신규로 생성되는 경로의 권한이 ROOT로 서비스계정에서는 접근하기 어려움
- 별도 방식필요(=>Mount Volumn)
- 서버의 docker로그 쌓이는 경로 확인
docker inspect -f {{.LogPath}} a584bdba7334

- docker log driver type 확인
docker inspect -f '{{.HostConfig.LogConfig.Type}}' a584bdba7334


##### 3-1) root그룹에 docker계정 포함시키기
>(root) usermod  -g root msapoc
-도커 재기동시마다 새로 생성되는 로그폴더 접근을 위해서는 root권한이 필요하다  
- https://studyforus.tistory.com/223
