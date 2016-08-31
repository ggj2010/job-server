package com.weimob.jobserver.core.reg.domain;

import com.weimob.jobserver.core.reg.constant.JobLockType;
import com.weimob.jobserver.core.reg.constant.JobStatus;
import com.weimob.jobserver.core.reg.constant.JobType;

/**
 * @author: kevin
 * @date 2016/08/18.
 */
public interface JobConfiguration {
    String getJobName();

    String getGroup();

    String getJobClass();

    JobType getJobType();

    JobStatus getJobStatus();

    String getCron();

    String getSystemId();

    String getIp();

    String getMethod();

    JobLockType getLockType();

    String getParameterTypes();
}
