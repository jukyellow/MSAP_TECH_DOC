# Logging (Spring Boot + Log4j2(or logback) + Slf4j )


### 0. log4j2, logback 모두 설정가능하지만, log4j2를 설정가능
    * log4j 사용시 logging 패키지 전체를 제거하고 진행해야되서, 일단 logback을 사용하도록함.
    * Spring Boot 기본 로깅 시스템이 logback을 사용

<hr />
<br>

## [Spring Boot + Logback + Slf4j 설정]


### 1. dependency : 추가없음
### 2. properties설정
```properties
#for color print
spring.output.ansi.enabled= always
#logging
logging.path= /logs
logging.file= ${spring.application.name}
```

### 3. java
``` java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
...
@RestController
public class RestController {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(RestController.class);

    @GetMapping("/config/reload")
    public String doReloadConfig() {
        LOGGER.debug(">>doReloadConfig2:"+reloadVal);
        ...
    }
}
```

### 4. logback-spring.xml 설정
```xml
<configuration>
    <!-- <property name="CONSOL_LOG_FORMAT" value="%5p \(%F[%M]:%L\) [%d{yyyy/MM/dd HH:mm:ss}] - %m%n" />
    <property name="DAILY_LOG_FORMAT" value="%n %d{yyyy/MM/dd HH:mm:ss} %-5p [%thread] %M\(%F:%L\) - %m  %n" />
    <property name="BASE_DIR" value="" />
    <property name="LEVEL" value="" />
    <property name="APP_NAME" value="aircargotrace2" />-->
    
    <appender name="CONSOLE_APPENDER" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%5p \(%F[%M]:%L\) [%d{yyyy/MM/dd HH:mm:ss}] - %m%n</pattern>
        </encoder>  
    </appender>
    
    <appender name="DAILY_APPENDER" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/daily/${LOG_FILE}.daily.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/daily/${LOG_FILE}.daily.%d{yyyyMMdd}.log
            </fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%n %d{yyyy/MM/dd HH:mm:ss} %-5p [%thread] %M\(%F:%L\) - %m  %n</pattern>
        </encoder>
    </appender>    
    <appender name="SQL_APPENDER" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/sql/${LOG_FILE}.sql.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/sql/${LOG_FILE}.sql.%d{yyyyMMdd}.log
            </fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%n %d{yyyy/MM/dd HH:mm:ss} %-5p [%thread] %M\(%F:%L\) - %m  %n</pattern>
        </encoder>
    </appender>


    <appender name="ASYNC_DAILY_APPENDER" class="ch.qos.logback.classic.AsyncAppender">
        <!-- Queue의 크기중에 비어있는 부분이 discardingThreshold 비율 이하인 경우 INFO 레벨 이하의 로그는 저장하지 않는다.
            모든 로그 이벤트를 처리하기 위해서는 0으로 discardingThreshold을 설정합니다 -->
        <discardingThreshold>0</discardingThreshold>        
        <queueSize>1024</queueSize> <!-- 큐의 최대 용량. 기본적으로 QUEUESIZE는 256으로 설정됩니다. -->
        <includeCallerData>true</includeCallerData> <!-- 해당 로그 이벤트 정보를 Queue에 추가하는 시점에 로그를 호출한 정보 (callerData)를 추출할지 여부를 결정하는 속성-->
        <!--<maxFlushTime>int</maxFlushTime> (밀리 초) 큐의 최대 flush timeout 시간을 지정합니다.처리 할 수??없는 이벤트가 삭제됩니다.-->
        <appender-ref ref="DAILY_APPENDER" />
    </appender>
    <appender name="ASYNC_SQL_APPENDER" class="ch.qos.logback.classic.AsyncAppender">
        <discardingThreshold>0</discardingThreshold>        
        <queueSize>1024</queueSize>
        <includeCallerData>true</includeCallerData>
        <appender-ref ref="SQL_APPENDER" />
    </appender>
    <appender name="ASYNC_CONSOL_APPENDER" class="ch.qos.logback.classic.AsyncAppender">
        <discardingThreshold>0</discardingThreshold>        
        <queueSize>1024</queueSize>
        <includeCallerData>true</includeCallerData>
        <appender-ref ref="CONSOLE_APPENDER" />
    </appender>
    
    <!-- Wrap calls to the console logger with async dispatching to Disruptor.
    <appender name="async" class="reactor.logback.AsyncAppender">
        <backlog>1024</backlog> Backlog size for logging events. Change size if they are picked up slowly. Default is 1024 * 1024
        <includeCallerData>false</includeCallerData> Caller data is relatively slow, so per default disabled
        <appender-ref ref="stdout"/>
    </appender>-->
    
    <!-- chapters.configuration 패키지의 하위 구성은 모두 level="INFO"로 적용 -->
    <logger name="com.msap" level="DEBUG" additivity="false">
        <appender-ref ref="ASYNC_DAILY_APPENDER"/>
        <appender-ref ref="ASYNC_CONSOL_APPENDER"/>
    </logger>    
    <!-- mybatis 쿼리를 남길려면, mapper class를 잡아서 찍어주면 된다. -->
    <logger name="com.msap.cargotrace.dao" level="DEBUG" additivity="false">
        <appender-ref ref="ASYNC_SQL_APPENDER"/>
        <appender-ref ref="ASYNC_CONSOL_APPENDER"/>
    </logger>
    
    <!-- Strictly speaking, the level attribute is not necessary since -->
    <!-- the level of the root level is set to DEBUG by default.       -->
    <root level="INFO">
        <appender-ref ref="ASYNC_CONSOL_APPENDER" />
    </root>
</configuration>
```

<hr />
<br>

## [Spring Boot + Log4j2 + Slf4j]

* 참고: https://madplay.github.io/post/spring-boot-log4j2


### 1. dependency
```groovy
//aphache.logging.log4j 전체를 제외함(default logback 패키지와 충돌발생, 부분제외로 해결안되었음.)
configurations {
    all*.exclude group: 'org.springframework.boot', module : 'spring-boot-starter-logging'
}
//log4j, slf4j 관련 dependency 추가
 implementation ('org.springframework.boot:spring-boot-starter-log4j2') //log4j2    
 implementation 'org.slf4j:slf4j-api'
 implementation 'org.apache.logging.log4j:log4j-slf4j-impl'
```

### 2. properties설정
```properties
#for color print
spring.output.ansi.enabled= always
#logging
logging.path= /logs
logging.file= ${spring.application.name}
```

### 3. log4j2.xml 정의
* 의존성 설정 이후에는 src/main/resources 경로에 log4j2.xml 파일을 생성합니다.
* XML이 아닌 프로퍼티 설정 파일로도 진행할 수 있는데 그 경우에는 log4j2.properties을 생성하면 됩니다.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="info" monitorInterval="30">
    <Properties>
        <Property name="LOG_FORMAT">%d{yyyy-MM-dd HH:mm:ss} %p %m%n</Property>
        <Property name="BASE_DIR">/Users/madplay/Desktop/bootdemo</Property>
    </Properties>

    <Appenders>
        <Console name="Console" target="SYSTEM_OUT" follow="true">
            <PatternLayout pattern="${LOG_FORMAT}"/>
        </Console>
        <RollingFile name="File"
                     fileName="${BASE_DIR}/bootdemo.log"
                     filePattern="${BASE_DIR}/bootdemo.%d{yyyyMMdd}.log">
            <PatternLayout pattern="${LOG_FORMAT}"/>
            <Policies>
                <TimeBasedTriggeringPolicy />
            </Policies>
            <DefaultRolloverStrategy>
                <Delete basePath="${BASE_DIR}">
                    <IfFileName glob="*.log" />
                    <IfLastModified age="30d" />
                </Delete>
            </DefaultRolloverStrategy>
        </RollingFile>
    </Appenders>

    <Loggers>
        <Root level="info">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="File" />
        </Root>
    </Loggers>
</Configuration>
```
* Log4j2 Async Appender
* 기타: <includeLocation>true</includeLocation>  # 전송시 에러예방:  https://docs.ncloud.com/ko/elsa/elsa-1-5-6.html
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="info" monitorInterval="30">
    <Properties>
        <Property name="CONSOL_LOG_FORMAT">%5p (%F[%M]:%L) [%d{yyyy/MM/dd HH:mm:ss}] - %m%n</Property>
        <Property name="DAILY_LOG_FORMAT">%n %d{yyyy/MM/dd HH:mm:ss} %-5p %x %M(%F:%L) - %m  %n</Property>        
        <Property name="BASE_DIR">/logs</Property>
        <Property name="LEVEL">debug</Property> <!-- ${zuul.logger.level} -->
        <Property name="APP_NAME">aircargotrace</Property>
    </Properties>

    <Appenders>
        <Console name="CONSOLE_APPENDER" target="SYSTEM_OUT" follow="true">
            <PatternLayout pattern="${CONSOL_LOG_FORMAT}"/>
        </Console>
        <!-- <appender name="CONSOLE_APPENDER" class="org.apache.log4j.ConsoleAppender">
            <layout class="org.apache.log4j.PatternLayout">
                <param name="ConversionPattern" value="%5p (%F[%M]:%L) [%d{yyyy/MM/dd HH:mm:ss}] - %m%n" />
            </layout>
        </appender> -->
        <RollingFile name="DAILY_APPENDER"
                     fileName="${BASE_DIR}/daily/${APP_NAME}.daily.log"
                     filePattern="${BASE_DIR}/daily/${APP_NAME}.daily.%d{yyyyMMdd}.log">
            <PatternLayout pattern="${DAILY_LOG_FORMAT}"/>
            <Policies>
                <TimeBasedTriggeringPolicy />
            </Policies>
            <DefaultRolloverStrategy>
                <Delete basePath="${BASE_DIR}">
                    <IfFileName glob="*.log" />
                    <IfLastModified age="30d" />
                </Delete>
            </DefaultRolloverStrategy>
        </RollingFile>
        <RollingFile name="SQL_APPENDER"
                     fileName="${BASE_DIR}/sql/${APP_NAME}.sql.log"
                     filePattern="${BASE_DIR}/sql/${APP_NAME}.sql.%d{yyyyMMdd}.log">
            <PatternLayout pattern="${DAILY_LOG_FORMAT}"/>
            <Policies>
                <TimeBasedTriggeringPolicy />
            </Policies>
            <DefaultRolloverStrategy>
                <Delete basePath="${BASE_DIR}">
                    <IfFileName glob="*.log" />
                    <IfLastModified age="30d" />
                </Delete>
            </DefaultRolloverStrategy>
        </RollingFile>
        <Async name="ASYNC_CONSOL_APPENDER" bufferSize = "1024" >
            <AppenderRef ref = "CONSOLE_APPENDER" /> <!--  inclueLocation="true" -->
        </Async>
        <Async name="ASYNC_DAILY_APPENDER" bufferSize = "1024">
            <AppenderRef ref = "DAILY_APPENDER" />
        </Async>
        <Async name="ASYNC_SQL_APPENDER" bufferSize = "1024">
            <AppenderRef ref = "SQL_APPENDER" />
        </Async>
    </Appenders>

    <Loggers>
        <Logger name="org.springframework" level="info" additivity="false">
            <AppenderRef ref="ASYNC_CONSOL_APPENDER"/>
        </Logger>
        <Logger name="com.msap" level="debug" additivity="false">
            <AppenderRef ref="ASYNC_CONSOL_APPENDER"/>
            <AppenderRef ref="ASYNC_DAILY_APPENDER"/>
        </Logger>
        <!-- mybatis 로그를 남길려면, mapper 클래스를 잡아서 저장하면 된다-->
        <Logger name="com.msap.cargotrace.dao" level="debug" additivity="false">
            <AppenderRef ref="ASYNC_CONSOL_APPENDER"/>
            <AppenderRef ref="ASYNC_SQL_APPENDER"/>
        </Logger>
        <Root level="info">        
            <AppenderRef ref="ASYNC_CONSOL_APPENDER"/>
        </Root>
    </Loggers>
</Configuration>
```

### 4. java code
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
...
@RestController
@RefreshScope
public class RestController {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(RestController.class);
            
    @Value("${test.reload.val}")
    private String reloadVal=null;
    
    @GetMapping("/config/reload")
    public String doReloadConfig() {
        System.out.println(">>doReloadConfig1:"+reloadVal);
        LOGGER.debug(">>doReloadConfig2:"+reloadVal);
        
        if(reloadVal==null) {
            return "null";
        }else {
            return "reloadVal:"+reloadVal;
        }
    }
}
```


* 참고:  https://howtodoinjava.com/log4j2/log4j2-properties-example/
* application.properties를 통한 로깅 설정
- log4j2.properties
<pre>
status = error
name = PropertiesConfig

#Make sure to change log file path as per your need
property.filename = C:\\logs\\debug.log
filters = threshold
filter.threshold.type = ThresholdFilter
filter.threshold.level = debug

appenders = rolling
appender.rolling.type = RollingFile
appender.rolling.name = RollingFile
appender.rolling.fileName = ${filename}
appender.rolling.filePattern = debug-backup-%d{MM-dd-yy-HH-mm-ss}-%i.log.gz
appender.rolling.layout.type = PatternLayout
appender.rolling.layout.pattern = %d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n
appender.rolling.policies.type = Policies
appender.rolling.policies.time.type = TimeBasedTriggeringPolicy
appender.rolling.policies.time.interval = 1
appender.rolling.policies.time.modulate = true
appender.rolling.policies.size.type = SizeBasedTriggeringPolicy
appender.rolling.policies.size.size=10MB
appender.rolling.strategy.type = DefaultRolloverStrategy
appender.rolling.strategy.max = 20
loggers = rolling

#Make sure to change the package structure as per your application
logger.rolling.name = com.howtodoinjava
logger.rolling.level = debug
logger.rolling.additivity = false
logger.rolling.appenderRef.rolling.ref = RollingFile
</pre>

* 기타 개별 패키지 제외 방법 gradle exclude: 
```groovy
dependencies {
    implementation('log4j:log4j:1.2.15') {
        exclude group: 'javax.jms', module: 'jms'
        exclude group: 'com.sun.jdmk', module: 'jmxtools'
        exclude group: 'com.sun.jmx', module: 'jmxri'
    }
}
```

* logback 관련 전체를 제외하고자 할때: 단, spring boot log자체가 기존형태와(logback)달라지고 log4j식으로 찍혀서 불편함
``` groovy
configurations {
    all*.exclude module : 'spring-boot-starter-logging'
}
```
