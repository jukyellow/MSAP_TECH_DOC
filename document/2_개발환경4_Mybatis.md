# Spring Boot DB연동 (Mybatis,Transaction,DB Pool)

* Gradle
![image](https://user-images.githubusercontent.com/45334819/61801594-04b13700-ae6a-11e9-9f95-a25bbba5a280.png)  

* properties(config서버-yml)  
![image](https://user-images.githubusercontent.com/45334819/71927625-39773780-31d9-11ea-9ee9-60bad8c9dcd5.png)  

* java interface
``` java
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;

@Mapper
public interface Db1Mapper {
    public int selec...(Map params);
    public int selec...(Map params);
    public Collection<? extends String> selectHblsOn...(Map params);
    public List<Map<String, Object>> select...(Map params);
    public List<Map<String, Object>> select...(Map params);    
    //test
    public int updateTest...(Map params);
}
```

* Bean (참고 : https://jdm.kr/blog/230)  
``` java 
//zuul-jdbc를 Primary로 선언함(로직에서는 사용안하고 zuul-jdbc로만 전달함)
@Configuration
@MapperScan(value="com.msap.cargotrace.dao.mapper1", sqlSessionFactoryRef="db1SqlSessionFactory")
@EnableTransactionManagement
public class Db1DataSourceConfig {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(Db1DataSourceConfig.class);

    @Bean
    @Primary
    @ConfigurationProperties(prefix="spring.db1.datasource")
    public PoolProperties getDb1PoolProperties() {
        return new PoolProperties();
    }

    @Bean(name = "db1DataSource", destroyMethod = "close")
    @Primary
    @ConfigurationProperties(prefix="spring.db1.datasource")
    public DataSource db1DataSource() {
        //DataSource ds = DataSourceBuilder.create().build();
        //org.apache.tomcat.jdbc.pool.DataSource
        DataSource ds = new DataSource(getDb1PoolProperties());
        LOGGER.info("[Db1DataSourceConfig] ds: " + ds);
        return ds;
    }

    @Bean(name = "db1SqlSessionFactory")
    @Primary
    public SqlSessionFactory db1SqlSessionFactory(
        @Qualifier("db1DataSource") DataSource db1DataSource, ApplicationContext appContext
        ) throws Exception {
        SqlSessionFactoryBean sqlSessFacBean = new SqlSessionFactoryBean();
        sqlSessFacBean.setDataSource(db1DataSource);
        sqlSessFacBean.setMapperLocations(appContext.getResources("classpath:mapper/*.xml"));
        return sqlSessFacBean.getObject();
    }

    @Bean(name = "db1SqlSessionTemplate")
    @Primary
    public SqlSessionTemplate db1SqlSessionTemplate(SqlSessionFactory db1SqlSessFactory) {
        return new SqlSessionTemplate(db1SqlSessFactory);
    }

    @Bean(name = "db1DataSourceTransactionManager")
    @Primary
    public DataSourceTransactionManager transactionManager(@Qualifier("db1DataSource")DataSource db1Datasource) {
        return new DataSourceTransactionManager(db1Datasource);
    }
}
```
* mapper xml쿼리문
```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.msap.....dao.mapper2.Db2Mapper">
        <select id="retrieveApiProcLogSeq" resultType="int">
                SELECT API_PROC_LOG_SEQ.NEXTVAL AS NEXT_API_PROC_LOG_SEQ
                  FROM DUAL
        </select>
```

* @Transactional
![image](https://user-images.githubusercontent.com/45334819/61801729-3c1fe380-ae6a-11e9-844e-8acd8392eadc.png)

* 참고:  https://uwostudy.tistory.com/19
![image](https://user-images.githubusercontent.com/45334819/56146162-b4ac1400-5fe0-11e9-802a-f6c62367c414.png)
![image](https://user-images.githubusercontent.com/45334819/56146212-d0171f00-5fe0-11e9-9260-08e551cac90d.png)

*  XML예)
![image](https://user-images.githubusercontent.com/45334819/61801748-4a6dff80-ae6a-11e9-9820-f3cb70422df0.png)  
<br>

* DB reconnection 설정(yml파일)  
```
#Zuul-Jdbc(maspoc)
spring:
  db1:
    datasource:
      driverClassName : oracle.jdbc.driver.OracleDriver
      url : jdbc:oracle:thin:@210.102.77.61:1521:cheetah
      username : msapoc #nlps
      password : msapoc_123 #nlps2012
      maxActive : 20
      maxIdle : 20
      minIdle : 10
      maxWait : 2000   #10000->2000
      initialSize : 10
      testOnBorrow: true       #reconnection mandatory  
      testWhileIdle : true
      validationQuery: SELECT * FROM DUAL # reconnection mandatory  
```
- 참고 : https://preamtree.tistory.com/78
- 설명
```
spring.datasource.tomcat.max-active=10     // 최대 connection 개수
spring.datasource.tomcat.initial-size=2    // 초기화 connection 개수    
spring.datasource.tomcat.max-idle=2        // idle 상태의 connection 최대 개수        
spring.datasource.tomcat.min-idle=1        // idle 상태의 connection 최소 개수        
// true로 주면, idle 상태에서도 test를 실행함
spring.datasource.tomcat.test-while-idle=true    
// idle 상태가 얼마나 지속되면 test를 실행할 것인지. mil값임.
spring.datasource.tomcat.time-between-eviction-runs-millis=3600000    
// connection의 유효기간.
spring.datasource.tomcat.min-evictable-idle-time-millis=7200000    
// connection을 가져올 때, test 할것인지 여부    
spring.datasource.tomcat.testOnBorrow=true        
// test QUERY
spring.datasource.tomcat.validationQuery=SELECT 1 
```

