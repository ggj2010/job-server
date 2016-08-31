package com.weimob.jobserver.core.job.log;

import com.weimob.jobserver.core.job.commond.CommondResult;

/**
 * @author: kevin
 * @date 2016/08/04.
 */
public class JobLogInfo extends CommondResult {
    private String jobName;
    private String systemId;
    private String groupName;
    private Long startTime;
    private Long endTime;
    private Boolean success;
    private String clientIp;
    private Integer clientPort;

    public JobLogInfo() {
    }

    public JobLogInfo(String systemId, String jobName, String groupName, Long startTime, String clientIp) {
        this.systemId = systemId;
        this.jobName = jobName;
        this.groupName = groupName;
        this.startTime = startTime;
        this.clientIp = clientIp;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Integer getClientPort() {
        return clientPort;
    }

    public void setClientPort(Integer clientPort) {
        this.clientPort = clientPort;
    }
}