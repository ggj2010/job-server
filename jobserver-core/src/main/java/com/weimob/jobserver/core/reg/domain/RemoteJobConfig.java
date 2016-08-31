package com.weimob.jobserver.core.reg.domain;

import com.weimob.jobserver.core.reg.constant.JobLockType;
import com.weimob.jobserver.core.reg.constant.JobStatus;
import com.weimob.jobserver.core.reg.constant.JobType;

/**
 * @author: kevin
 * @date 2016/08/18.
 */

public class RemoteJobConfig extends LocalJobConfig {

    private String clientName;

    public RemoteJobConfig() {
    }

    public RemoteJobConfig(String jobName, String group, String cron, String jobClass, JobType jobType, JobStatus jobStatus, String systemId, String ip, JobLockType lockType, String clientName) {
        super(jobName, group, cron, jobClass, jobType, jobStatus, systemId, ip, lockType);
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}
