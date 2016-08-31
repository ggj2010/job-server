package com.weimob.jobserver.server.job.database.baen;

import com.weimob.jobserver.server.job.database.base.bean.BaseEntity;

import java.util.Date;

/**
 * @author: kevin
 * @date 2016/08/11.
 */
public class JobLogBean extends BaseEntity {
    private Integer jobId;
    private Integer serverId;
    private Long startTime;
    private Long endTime;
    private boolean success;
    private Date updateTime;
    private Integer pageSize;
    private Integer pageNum;

    public JobLogBean() {
    }

    public JobLogBean(Integer jobId, Integer serverId) {
        this.jobId = jobId;
        this.serverId = serverId;
        this.success = false;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

}
