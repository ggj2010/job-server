package com.weimob.jobserver.server.job.database.baen;

import com.weimob.jobserver.core.reg.constant.JobStatus;
import com.weimob.jobserver.server.job.database.base.bean.BaseEntity;

import java.util.List;

/**
 * @author: kevin
 * @date 2016/08/11.
 */
public class JobConfigEntity extends BaseEntity {
    private String systemId;
    private String jobClass;
    private String methodName;
    private String parameterTypes;
    private Integer status;//运行状态: 1 正在运行  2 暂停运行  3 已经删除
    private Integer jobType;
    private String jobName;
    private String cronExpression;
    private String groupName;
    private Integer lockStratergy;

    private List<ServerConfigEntity> serverConfigList;

    private Integer serverCount;//服务器数量（数据库没有该字段）

    public JobConfigEntity() {
    }

    public JobConfigEntity(String jobName, String systemId) {
        this.jobName = jobName;
        this.systemId = systemId;
    }

    public JobConfigEntity(String systemId, String jobClass, JobStatus status, Integer jobType, String jobName, String cronExpression, String groupName, Integer lockStratergy) {
        this.jobClass = jobClass;
        this.systemId = systemId;
        this.status = status.getStatus();
        this.jobType = jobType;
        this.jobName = jobName;
        this.cronExpression = cronExpression;
        this.groupName = groupName;
        this.lockStratergy = lockStratergy;
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

    public Integer getJobType() {
        return jobType;
    }

    public void setJobType(Integer jobType) {
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

    public Integer getLockStratergy() {
        return lockStratergy;
    }

    public void setLockStratergy(Integer lockStratergy) {
        this.lockStratergy = lockStratergy;
    }

    public List<ServerConfigEntity> getServerConfigList() {
        return serverConfigList;
    }

    public void setServerConfigList(List<ServerConfigEntity> serverConfigList) {
        this.serverConfigList = serverConfigList;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public Integer getServerCount() {
        return serverCount;
    }

    public void setServerCount(Integer serverCount) {
        this.serverCount = serverCount;
    }

    public boolean isSame(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobConfigEntity that = (JobConfigEntity) o;

        if (systemId != null ? !systemId.equals(that.systemId) : that.systemId != null) return false;
        if (jobClass != null ? !jobClass.equals(that.jobClass) : that.jobClass != null) return false;
        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;
        if (parameterTypes != null ? !parameterTypes.equals(that.parameterTypes) : that.parameterTypes != null)
            return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (jobType != null ? !jobType.equals(that.jobType) : that.jobType != null) return false;
        if (jobName != null ? !jobName.equals(that.jobName) : that.jobName != null) return false;
        if (cronExpression != null ? !cronExpression.equals(that.cronExpression) : that.cronExpression != null)
            return false;
        if (groupName != null ? !groupName.equals(that.groupName) : that.groupName != null) return false;
        return lockStratergy != null ? lockStratergy.equals(that.lockStratergy) : that.lockStratergy == null;

    }

    @Override
    public String toString() {
        return "JobConfigEntity{" +
                "systemId='" + systemId + '\'' +
                ", jobClass='" + jobClass + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterTypes='" + parameterTypes + '\'' +
                ", status=" + status +
                ", jobType=" + jobType +
                ", jobName='" + jobName + '\'' +
                ", cronExpression='" + cronExpression + '\'' +
                ", groupName='" + groupName + '\'' +
                ", lockStratergy=" + lockStratergy +
                ", serverConfigList=" + serverConfigList +
                ", serverCount=" + serverCount +
                '}';
    }
}
