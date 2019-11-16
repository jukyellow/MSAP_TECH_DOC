#  Docker 모니터링  

### 1. Docker stats

#### 1-1. 실행  
- 참고: https://waspro.tistory.com/531  
```
[root@elk ~]# docker stats 
CONTAINER           CPU %               MEM USAGE / LIMIT       MEM %               NET I/O             BLOCK I/O           PIDS 
fc2fb010cf90        0.01%               6.395 MiB / 7.637 GiB   0.08%               1.31 kB / 656 B     0 B / 0 B           6 
af56fd091b95        0.00%               102.7 MiB / 7.637 GiB   1.31%               656 B / 656 B       0 B / 39.2 MB       2 
```
#### 1-2. 옵션  
```
Usage:  docker stats [OPTIONS] [CONTAINER...] 
Display a live stream of container(s) resource usage statistics 
Options: 
  -a, --all             Show all containers (default shows just running) 
      --format string   Pretty-print images using a Go template 
      --help            Print usage 
      --no-stream       Disable streaming stats and only pull the first result 
```

#### 1-3. 설명 
- CPU, MEM은 할당된 Resource 대비 사용률  
- NET I/O는 Network Interface를 통해 전송하고 받는 데이터 량  
- BLOCK I/O는 블록 디스크에 읽고 쓴 데이터 량  
- PIDs는 컨테이너가 생성한 프로세스 또는 쓰레드 수를 의미    
