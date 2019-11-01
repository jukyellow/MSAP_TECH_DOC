# Zuul UUID생성 및 Cookie로 서비스전달 방법

### 1. request 추적을 위해 UUID공유 방법:

- 작업순서:  
1. zuul에서 UUID생성
2. Cookie저장
```
RequestContext ctx = RequestContext.getCurrentContext()획득
ctx.addZuulRequestHeader("Cookie", "SESSION=" + uuid);
```
3. 개별 서비스에서 Cookie획득
```
Cookie[] cookieList = request.getCookies();
String ckUUID = null;
for(Cookie ck : cookieList) {
    if(ApiCommon.SessionUUID.equals(ck.getName())){
        ckUUID = ck.getValue();
    }
}
```
