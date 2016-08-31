package com.weimob.jobserver.server.job.executor.selection;

public class Server {
    public String ip;
    public int weight;
    private Integer serverId;

    public Server(String ip, int weight, Integer serverId) {
        super();
        this.ip = ip;
        this.weight = weight;
        this.serverId = serverId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }
}