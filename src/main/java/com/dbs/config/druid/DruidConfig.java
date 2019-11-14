package com.dbs.config.druid;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.dbs.config.datasource.DynamicDataSourceToChoose;
import com.google.common.collect.Lists;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

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
