package com.weimob.jobserver.core.reg.annotation;

import com.weimob.jobserver.core.reg.constant.JobLockType;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author: kevin
 * @date 2016/08/18.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface ScheduledJob {
    String jobName();

    String cronExp() default "";

    String group() default "DEFAULT";

    JobLockType LOCK_TYPE() default JobLockType.OVVERAL_UNIQUE;//锁机制，是否对定时任务进行加锁

}
