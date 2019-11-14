package com.dbs.config.datasource;

import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DBtype {

    String dataSource() default "";
}
