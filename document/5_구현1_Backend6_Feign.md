# Feign 

- 참고: https://comnic.tistory.com/23

1)A,B 각각 서비스가 존재한다고 가정하고, B서비스의 Rest URI 부분
```
@RestController
public class CommonController {
  private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CommonController.class);
  
  @RequestMapping(value="/common/log", method = RequestMethod.POST)
  public String printCommonLog(HttpServletRequest request, HttpServletResponse response) throws Exception {
    LOGGER.info("[CommonController] /common/log called");
    return "/common/log called";
  }
}
```

2) A→B서비스 호출시: A서비스에 Feign 인터페이스 클래스 생성
```
@FeignClient(name="msap-feign-common", url = "${feign.common.url}") //http:localhost:8201
public interface FeignCommonTest {
  @RequestMapping(value="/common/log", method=RequestMethod.POST)
  public String commonLog();
}
```

3)Feign 인터페이스 클래스를 통해 A→B Rest URI 호출
 > dependency 추가(gradle) : implementation 'org.springframework.cloud:spring-cloud-starter-openfeign' //openfeign
```
@RestController
public class SmartLogisApiControllerFeign {
  private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SmartLogisApiControllerFeign.class);

  @Autowired
  private FeignCommonTest feignTest;
  @RequestMapping(value = ApiCommon.API_URL_FEIGN_TEST, method = RequestMethod.POST)
  public String commonLogPost(HttpServletRequest request, HttpServletResponse response) throws Exception {
    LOGGER.info(" >>> commonLogPost called");
    return feignTest.commonLog();
  }
}
```

