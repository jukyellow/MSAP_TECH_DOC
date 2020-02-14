# 관리해야할 도커로그 유형


### 1. application 로그: application→file로 쓰는 로그  
1-1. spring boot 기반 사용자구현 application  
- application의 로깅엔진(spring boot + log4j등)으로 로깅정책 정의   
  + 도커 볼륨설정(내부 저장소→host서버 저장 및 컨테이너 재기동간에도 유지가능하게 함)  

1-2. 도커 이미지 기반 application (ex: 엘라스틱 서치)  
- 개별 application 설정의 로그백업 주기등을 설정하여 적용  
- 참고: https://www.elastic.co/guide/en/elasticsearch/reference/master/logging.html#logging  
- 설정파일 path : $ES_HOME/config/log4j2.properties
```
######## Server JSON ############################
appender.rolling.type = RollingFile
appender.rolling.name = rolling
appender.rolling.fileName = ${sys:es.logs.base_path}${sys:file.separator}${sys:es.logs.cluster_name}_server.json
appender.rolling.layout.type = ESJsonLayout
appender.rolling.layout.type_name = server
appender.rolling.filePattern = ${sys:es.logs.base_path}${sys:file.separator}${sys:es.logs.cluster_name}-%d{yyyy-MM-dd}-%i.json.gz
appender.rolling.policies.type = Policies
appender.rolling.policies.time.type = TimeBasedTriggeringPolicy
appender.rolling.policies.time.interval = 1
appender.rolling.policies.time.modulate = true
appender.rolling.policies.size.type = SizeBasedTriggeringPolicy
appender.rolling.policies.size.size = 256MB
appender.rolling.strategy.type = DefaultRolloverStrategy
appender.rolling.strategy.fileIndex = nomax
appender.rolling.strategy.action.type = Delete
appender.rolling.strategy.action.basepath = ${sys:es.logs.base_path}
appender.rolling.strategy.action.condition.type = IfFileName
appender.rolling.strategy.action.condition.glob = ${sys:es.logs.cluster_name}-*
appender.rolling.strategy.action.condition.nested_condition.type = IfAccumulatedFileSize
appender.rolling.strategy.action.condition.nested_condition.exceeds = 2GB
################################################
```
<br>

### 2. 도커 컨테이너 stdout 로그: 도커 컨테이너 구동 application→ stdout(표준출력 로그)→ host의 /var/lib/docker/containers/하위에 저장됨  
- log-driver 설정으로 관리하는 방법(1):  도커 컨테이너 실행시 명령어 세팅 or compose파일에 정의  
- host서버 rotate설정으로 관리하는 방법(2): logrotate설정하여 관리  
<br>

### 3. 호스트 서버 LogRotate설정
```
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
