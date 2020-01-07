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

