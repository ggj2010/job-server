package com.weimob.jobserver.server.job.database.base.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface MyBatisDao {
    String value() default "";
}