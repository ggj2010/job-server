package com.weimob.jobserver.core.job.commond;

import java.io.Serializable;

/**
 * 控制本地任务的命令
 *
 * @author: kevin
 * @date 2016/08/14.
 */
public class JobCommond implements Serializable {
    private CommondType commondType;
    private String jobName;
    private String group;
    private String systemId;
    private String ip;
    private Integer port;

    public JobCommond() {
    }

    public JobCommond(CommondType commondType, String systemId, String jobName, String group) {
        this.commondType = commondType;
        this.systemId = systemId;
        this.jobName = jobName;
        this.group = group;
    }

    public JobCommond(CommondType commondType) {
        this.commondType = commondType;
    }

    public CommondType getCommondType() {
        return commondType;
    }

    public void setCommondType(CommondType commondType) {
        this.commondType = commondType;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Override
    public String toString() {
        return "JobCommond{" +
                "commondType=" + commondType +
                ", jobName='" + jobName + '\'' +
                ", group='" + group + '\'' +
                ", systemId='" + systemId + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
