package com.weimob.jobserver.server.job.database.baen;

import com.weimob.jobserver.server.job.database.base.bean.BaseEntity;

/**
 * @author: kevin
 * @date 2016/08/22.
 */
public class ServerConfigEntity extends BaseEntity {
    private Integer jobId;
    private String clientName;
    private String serverIp;
    private Integer serverPort;
    private Integer weight;
    private Integer serverStatus;

    public ServerConfigEntity() {
    }

    public ServerConfigEntity(Integer jobId) {
        this.jobId = jobId;
    }

    public ServerConfigEntity(Integer jobId, String serverIp, Integer weight) {
        this.jobId = jobId;
        this.serverIp = serverIp;
        this.weight = weight;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(Integer serverStatus) {
        this.serverStatus = serverStatus;
    }
}
