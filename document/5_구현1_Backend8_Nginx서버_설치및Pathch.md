# Nginx 웹서버 서버 설치 및 Pathch(health check 기능 추가)

### nginx 기본설정
https://github.com/jukyellow/msa-side-proj/blob/master/document/2_%EA%B0%9C%EB%B0%9C%ED%99%98%EA%B2%BD5_Ngnix.md

### 개요
- nginx -> zuul (/health)경로로 사용가능여부 확인후 트래픽 전달

### 레퍼런스
- 참고1: https://blog.naver.com/PostView.nhn?blogId=kletgdgo&logNo=221368660917&categoryNo=0&parentCategoryNo=0&viewDate=&currentPage=1&postListTopCurrentPage=1&from=postView
- 참고2:  https://docs.nginx.com/nginx/admin-guide/load-balancer/http-health-check/
- 패치버전: https://github.com/yaoweibin/nginx_upstream_check_module

### 1. Nignx Patch 버전 install
- 참고 : https://byd0105.tistory.com/9
```
--1)path 최신버전: 1.16.1 다운로드
wget 'http://nginx.org/download/nginx-1.16.1.tar.gz'
tar -xzvf nginx-1.16.1.tar.gz

--2)patch-version git clone
git clone https://github.com/yaoweibin/nginx_upstream_check_module.git

--3)pathch 수행
cd nginx-1.16.1/
patch -p1 < ../nginx_upstream_check_module/check_1.16.1+.patch

--4)유틸 추가 설치: gcc/g++, PCRE, ZLIB (configure시 필요함)
sudo apt-get install gcc g++
sudo apt-get install libpcre3 libpcre3-dev
sudo apt-get install zlib1g-dev

--5)make install
sudo apt-get install make

--6)
#./configure --add-module=../nginx_upstream_check_module
./configure --add-module=../nginx_upstream_check_module --user=www-data --group=www-data

--7)유저제한? (이미 설정되있을수 있음)
useradd --shell /usr/sbin/nologin www-data

make
sudo make install

--8)배포경로: /usr/local/nginx/conf/

--9)service 인터페이스에 등록
#copy/download/curl/wget the init script
sudo wget https://raw.githubusercontent.com/JasonGiedymin/nginx-init-ubuntu/master/nginx -O /etc/init.d/nginx
sudo chmod +x /etc/init.d/nginx

sudo update-rc.d -f nginx defaults

sudo service nginx status  # to poll for current status
sudo service nginx stop    # to stop any servers if any
sudo service nginx start   # to start the server

#[optional remove the upstart script]
#sudo update-rc.d -f nginx remove

--10) 배포경로 권한추가
mkdir /usr/local/nginx/conf/conf.d
sudo chown -R msapoc nginx

--11) service 명령어
#start : sudo service nignx start
#stop : sudo service nignx stop
#reload : sudo service nignx reload
#status : sudo service nignx status
```

### 2. 배포경로
```
1. 설치파일 다운로드
> /msapoc/backend/deploy/20191204/patch_nginx

2. 설치 경로
> /usr/local/nginx

3. config 배포경로 및 파일
> /usr/local/nginx/conf/nginx.conf
> /usr/local/nginx/conf/conf.d/default.conf
```

### 3. Log Rotate설정
- 참고 : https://extrememanual.net/10139
```
1.전역 off
access_log off;
log_not_found off;
error_log /var/log/nginx/error.log crit;


2. 도메일별 설정추가
server {
   ...
   access_log /var/log/nginx/도메인/access.log
   ...
}

3. 로그 Rotate 설정
- 30일 보관 및 날짜 백업(``따옴표 주의!)
# /etc/logrotate.d/nignx (Ubuntu 16.0.4, nginx patch 16.1)  
/usr/local/nginx/logs/*.log {
    daily
    dateext
    dateyesterday
    missingok
    compress
    delaycompress
    rotate 30
    notifempty
    create 0640 msapoc root
    sharedscripts
    postrotate
        if [ -f /usr/local/nginx/logs/nginx.pid ]; then
            kill -USR1 `cat /usr/local/nginx/logs/nginx.pid`
        fi
    endscript
}
> Nginx는 새 로그파일을 생성할때, kill signal USR1을 받아서 처리함  


4. 크론탭 설정
> 00시 00분 정각에 동작시키기
: 아래 설정파일이 00 0으로 되어있는지 확인 및 설정

# /etc/crontab: system-wide crontab
# Unlike any other crontab you don't have to run the `crontab'
# command to install the new version when you edit this file
# and files in /etc/cron.d. These files also have username fields,
# that none of the other crontabs do.

SHELL=/bin/sh
PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin

# m h dom mon dow user  command
00 *    * * *   root    cd / && run-parts --report /etc/cron.hourly
00 0    * * *   root    test -x /usr/sbin/anacron || ( cd / && run-parts --report /etc/cron.daily )
00 0    * * 7   root    test -x /usr/sbin/anacron || ( cd / && run-parts --report /etc/cron.weekly )
00 0    1 * *   root    test -x /usr/sbin/anacron || ( cd / && run-parts --report /etc/cron.monthly )
```

### 4. Zuul Health Check Controller
```
@Controller
public class HealthController {
    @Autowired(required=false)
    private HealthEndpoint healthEndpoint;

    @RequestMapping("/health")
    @ResponseBody
    @ConditionalOnBean(value={HealthEndpoint.class})
    public ResponseEntity<String> health() {
        Health health = healthEndpoint.invoke();
        Status status = health.getStatus();
        
        if(status.equals(Status.UP)) { // -> 별도로 정의한 내부 상태값으로 대체(초기화 후 UP상태로 변경)
            HttpHeader header...
        
            return mew ResponseEntity("", header, HttpStatus.OK); //https 상태코드 200 리턴
        } else {
            throw mew ResponseEntity("", header, HttpStatus.UNAVAILABLE_SERVICE); //https 상태코드 503 리턴
        }        
    }
}
```

### 5. Nginx health check sample
- upstream 설정에 추가
```
check interval=2000 rise=5 fall=1 timeout=1000 type=http;
check_http_send "GET /zuul_health HTTP/1.0\r\n\r\n";
check_http_expect_alive http_2xx;
```
