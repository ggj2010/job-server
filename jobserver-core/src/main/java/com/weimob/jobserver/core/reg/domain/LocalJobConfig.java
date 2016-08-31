package com.weimob.jobserver.core.reg.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.weimob.jobserver.core.reg.constant.JobLockType;
import com.weimob.jobserver.core.reg.constant.JobStatus;
import com.weimob.jobserver.core.reg.constant.JobType;

/**
 * @author: kevin
 * @date 2016/08/18.
 */
public class LocalJobConfig implements JobConfiguration {
    private String jobName;
    private String group;
    private String jobClass;
    private JobType jobType;
    private JobStatus jobStatus;
    private String cron;
    private String systemId;
    private String ip;
    private String method;
    private JobLockType lockType;//锁机制
    private String parameterTypes;

    public LocalJobConfig() {
    }

    public LocalJobConfig(String jobName, String group, String cron, String jobClass, JobType jobType, JobStatus jobStatus, String systemId, String ip, JobLockType lockType) {
        this.jobName = jobName;
        this.group = group;
        this.jobClass = jobClass;
        this.cron = cron;
        this.jobType = jobType;
        this.jobStatus = jobStatus;
        this.systemId = systemId;
        this.ip = ip;
        this.lockType = lockType;
    }

    @Override
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String getJobClass() {
        return jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }

    @Override
    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    @Override
    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    @Override
    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    @Override
    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Override
    @JsonIgnore
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public JobLockType getLockType() {
        return lockType;
    }

    public void setLockType(JobLockType lockType) {
        this.lockType = lockType;
    }

    @Override
    public String getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(String parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
}
