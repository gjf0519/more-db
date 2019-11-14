package com.dbs.config.datasource;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

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
