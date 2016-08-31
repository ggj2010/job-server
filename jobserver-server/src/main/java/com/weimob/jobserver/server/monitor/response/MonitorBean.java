package com.weimob.jobserver.server.monitor.response;

import com.weimob.jobserver.server.job.database.baen.JobLogBean;

/**
 * @author: kevin
 * @date 2016/08/16.
 */
public class MonitorBean extends JobLogBean {
    private Integer executeCount;
    private Integer failCount;

    private String systemId;
    private String jobName;

    public Integer getExecuteCount() {
        return executeCount;
    }

    public void setExecuteCount(Integer executeCount) {
        this.executeCount = executeCount;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
}
