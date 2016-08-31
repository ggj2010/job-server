package com.weimob.jobserver.server.monitor.response;

import java.io.Serializable;

/**
 * @author: kevin
 * @date 2016/08/22.
 */
public class JobListResponse implements Serializable {
    private Integer id;
    private String systemId;
    private String jobClass;
    private String methodName;
    private String parameterTypes;
    private Integer status;//运行状态: 1 正在运行  2 暂停运行  3 已经删除
    private String statusStr;
    private String jobType;
    private String jobName;
    private String cronExpression;
    private String groupName;
    private String lockStratergy;
    private Integer level;
    private Integer avaliableCount;
    private Integer serverCount;


    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getJobClass() {
        return jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(String parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getLockStratergy() {
        return lockStratergy;
    }

    public void setLockStratergy(String lockStratergy) {
        this.lockStratergy = lockStratergy;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getStatusStr() {
        return statusStr;
    }

    public void setStatusStr(String statusStr) {
        this.statusStr = statusStr;
    }

    public Integer getAvaliableCount() {
        return avaliableCount;
    }

    public void setAvaliableCount(Integer avaliableCount) {
        this.avaliableCount = avaliableCount;
    }

    public Integer getServerCount() {
        return serverCount;
    }

    public void setServerCount(Integer serverCount) {
        this.serverCount = serverCount;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
