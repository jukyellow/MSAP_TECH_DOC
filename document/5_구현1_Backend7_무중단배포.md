# 무중단 배포(Zero Downtiome deployment)

### 적용방안
#### 1. 문제점: 재기동(도커 down,run)간 서비스가 종료된 이후, 또는 정상 구동후 처리준비가 완료된 이전에 트래픽이 전달되어 "Service Unavailable(503)" 발생
#### 2. 개선방안1-개별서비스:
- 서비스 down시: Eureka서버로 미리 서비스 down을 통지(1) + Eureka목록에서 제거(2) + 톰캣서버에 쌓여있는 request 처리할때까지 대기(3) + 서버종료  
- 서비스 up시: 서비스 구동직후 -> 서비스상태 Starting((1), Up상태가 아니면 처리불가상태) →  DB초기화(2) →  JVM기동완료까지 대기(3) → 자기자신으로 Request 샘플전송((4), 정상처리여부확인 + 잔여초기화수행) -> 서비스 up 통지(5)  
#### 개선방안2- 게이트웨이(zuul)
- 문제점: 서버 재기동간에 nginx웹서버에서 트래픽을 계속 전송하여 문제가됨  
- 개선방안: zuul 서버 재기동시, 웹서버(nginx)가 상태정보를 인식하고 트랙픽 차단하도록 함  
  nginx_upstream_check_module로 patch하고 nginx를 컴파일해서 적용함 → Health Check기능 사용할수 있음(Nginx에서 Health_Check모듈은 상용버전에서 사용가능)  
  도커파일 대신 nginx서버를 직접 서버에 설치해서 사용  
  
  
