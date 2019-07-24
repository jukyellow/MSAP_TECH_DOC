# Spring Boot + Mybatis + Transaction 관리

* Gradle
![image](https://user-images.githubusercontent.com/45334819/61801594-04b13700-ae6a-11e9-9f95-a25bbba5a280.png)  

* properties
![image](https://user-images.githubusercontent.com/45334819/61801712-332f1200-ae6a-11e9-86b3-75dcf341565a.png)

* java interface
``` java
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;

@Mapper
public interface Db1Mapper {
    public int selectHblsCntOnAirCargoTraceAll(Map params);
    public int selectHblsCntOnAirCargoTrace(Map params);
    public Collection<? extends String> selectHblsOnAirCargoTrace(Map params);
    public List<Map<String, Object>> selectMblAirCargoTrace(Map params);
    public List<Map<String, Object>> selectHblAirCargoTrace(Map params);
    
    //test
    public int updateTestHawbMtab(Map params);
}
```

* Bean
``` java 
//zuul-jdbc를 Primary로 선언함(로직에서는 사용안하고 zuul-jdbc로만 전달함)
@Configuration
@MapperScan(value="com.msap.cargotrace.dao.mapper1", sqlSessionFactoryRef="db1SqlSessionFactory")
@EnableTransactionManagement
public class Db1DataSourceConfig {
    @Bean(name = "db1DataSource", destroyMethod = "close")
    @Primary
    @ConfigurationProperties(prefix="spring.db1.datasource")
    public DataSource db1DataSource() {
        return DataSourceBuilder.create().build();
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

* @Transactional
![image](https://user-images.githubusercontent.com/45334819/61801729-3c1fe380-ae6a-11e9-844e-8acd8392eadc.png)

* 참고:  https://uwostudy.tistory.com/19
![image](https://user-images.githubusercontent.com/45334819/56146162-b4ac1400-5fe0-11e9-802a-f6c62367c414.png)
![image](https://user-images.githubusercontent.com/45334819/56146212-d0171f00-5fe0-11e9-9260-08e551cac90d.png)

*  XML예)
![image](https://user-images.githubusercontent.com/45334819/61801748-4a6dff80-ae6a-11e9-9820-f3cb70422df0.png)

