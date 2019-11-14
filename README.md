## 实现步骤
- 1、引入jar包：
<br>
springAOP需要使用：
<br>
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.9.4</version>
</dependency>
<br>
druid连接池使用：
<br>
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.1.19</version>
</dependency>
或者
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.1.18</version>
</dependency>
<br>
- 2、排除自动加载的类
<br>
DataSourceAutoConfiguration.class
<br>
或者
<br>
DruidDataSourceAutoConfigure.class、DataSourceAutoConfiguration.class
- 3、扩展spring的AbstractRoutingDataSource类
<br>
public class DynamicDataSourceToChoose extends AbstractRoutingDataSource {

    /**
     * 根据Key获取数据源的信息
     * @return
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return HandlerDataSource.getDataSource();
    }
}
- 4、定义切入点（注解）
<br>
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DBtype {

    String dataSource() default "";
}
- 5、定义切面，拦截自定义的注解
<br>
/**
 * @author gaijf
 * @description AOP拦截切换数据源注解
 * @date 2019/9/6
 */
@Slf4j
@Aspect
@Order(1)
@Component
public class HandlerDataSourceAop {

    /**
     * @within在类上设置
     * @annotation在方法上进行设置
     */
    @Pointcut("@within(com.dbs.config.datasource.DBtype)||@annotation(com.dbs.config.datasource.DBtype)")
    public void pointcut() {}

    @Before("pointcut()")
    public void doBefore(JoinPoint joinPoint)
    {
        Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
        DBtype annotationClass = method.getAnnotation(DBtype.class);//获取方法上的注解
        if(annotationClass == null){
            annotationClass = joinPoint.getTarget().getClass().getAnnotation(DBtype.class);//获取类上面的注解
            if(annotationClass == null) return;
        }
        //获取注解上的数据源的值的信息
        String dataSourceKey = annotationClass.dataSource();
        if(dataSourceKey !=null){
            //给当前的执行SQL的操作设置特殊的数据源的信息
            HandlerDataSource.putDataSource(dataSourceKey);
        }
        String dataSourceName = dataSourceKey==""?"direct":dataSourceKey;
        log.info("AOP动态切换数据源，类:{} 方法:{} 数据源:{}",joinPoint.getTarget().getClass().getName(),method.getName(),dataSourceName);
    }

    @After("pointcut()")
    public void after(JoinPoint point) {
        //清理掉当前设置的数据源，让默认的数据源不受影响
        HandlerDataSource.clear();
    }
}
- 6、线程与数据源绑定
<br>
/**
 * @author gaijf
 * @description 根据当前线程来选择具体的数据源
 * @date 2019/9/6
 */
public class HandlerDataSource {

    private static ThreadLocal<String> handlerThredLocal = new ThreadLocal<String>();

    /**
     * 设置当前使用的数据源
     * @param datasource
     */
    public static void putDataSource(String datasource) {
        handlerThredLocal.set(datasource);
    }

    /**
     * 取出当前要使用的数据源
     * @return
     */
    public static String getDataSource() {
        return handlerThredLocal.get();
    }

    /**
     * 使用默认的数据源
     */
    public static void clear() {
        handlerThredLocal.remove();
    }
}
- 7、定义数据源属性Bean
<br>
/**
 * @author gaijf
 * @description Druid配置类
 * @date 2019/9/6
 */
public class DruidProperties {
    private int  initialSize;
    private int minIdle;
    private int maxActive;

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }
}
- 8、将数据源Bean注入IOC容器
<br>
@Configuration
public class DruidConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    public DruidProperties getDruidProperties(){
        return new DruidProperties();

    }
    @Bean("direct")
    @ConfigurationProperties(prefix = "spring.datasource.db1")
    public DruidDataSource localDatasource(){
        DruidDataSource druidDataSource = new DruidDataSource();
        setProperties(druidDataSource);
        setFilter(druidDataSource);
        return druidDataSource;
    }

    @Bean("reptile")
    @ConfigurationProperties(prefix = "spring.datasource.db2")
    public DruidDataSource reptileDatasource(){
        DruidDataSource druidDataSource = new DruidDataSource();
        setProperties(druidDataSource);
        setFilter(druidDataSource);
        return druidDataSource;
    }

    private void setProperties(DruidDataSource druidDataSource){
        DruidProperties druidProperties = getDruidProperties();
        druidDataSource.setMinIdle(druidProperties.getMinIdle());
        druidDataSource.setMaxActive(druidProperties.getMaxActive());
        druidDataSource.setInitialSize(druidProperties.getInitialSize());
    }

    private void setFilter(DruidDataSource druidDataSource){
        druidDataSource.setProxyFilters(Lists.newArrayList(statFilter()));
    }

    @Bean
    @Primary
    public DataSource getDynamicDataSourceToChoose(){
        DynamicDataSourceToChoose choose = new DynamicDataSourceToChoose();
        Map<Object, Object> targetDataSources = new HashMap<>();
        DruidDataSource direct = localDatasource();
        DruidDataSource reptile = reptileDatasource();
        targetDataSources.put("direct",direct);
        targetDataSources.put("reptile",reptile);
        choose.setTargetDataSources(targetDataSources);
        choose.setDefaultTargetDataSource(direct);
        return choose;
    }

    @Bean
    public Filter statFilter(){
        StatFilter filter = new StatFilter();
        //定义多长时间为慢SQL
        filter.setSlowSqlMillis(5000);
        //是否记录日志
        filter.setLogSlowSql(true);
        //是否合并日志
        filter.setMergeSql(true);
        return filter;
    }

    @Bean
    public ServletRegistrationBean servletRegistrationBean(){
        return new ServletRegistrationBean(new StatViewServlet(),"/druid/*");
    }
}
- 9、properties添加配置信息
<br>
spring.datasource.druid.initialSize=5
spring.datasource.druid.minIdle=5
spring.datasource.druid.maxActive=20

spring.datasource.db1.type=com.alibaba.druid.pool.DruidDataSource
spring.datasource.db1.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.db1.url=jdbc:mysql://10.10.51.225:3306/yaodu_simp?useUnicode=true&characterEncoding=utf-8
spring.datasource.db1.username=gjf
spring.datasource.db1.password=Gjf@123456

spring.datasource.db2.type=com.alibaba.druid.pool.DruidDataSource
spring.datasource.db2.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.db2.url=jdbc:mysql://10.10.16.96:3306/xxl_job?useUnicode=true&characterEncoding=utf-8
spring.datasource.db2.username=root
spring.datasource.db2.password=123456
- 10、拦截方法上添加自定义注解
<br>
@DBtype(dataSource = "reptile")
public User getReptileUserById(int id){
    User user = userMapper.getUserById(id);
    log.info("根据ID查询用户user:{}", JSON.toJSONString(user));
    return user;
}
- 11、需要学习的地方
<br>
springAOP切面编程、springJDBC运行原理
- 12、需要注意的地方
<br>
如果与Mybatis集成，Mybatis开启二级缓存后切换数据源失效