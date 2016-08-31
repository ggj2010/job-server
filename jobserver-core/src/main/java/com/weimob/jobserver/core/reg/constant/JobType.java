package com.weimob.jobserver.core.reg.constant;

/**
 * @author: kevin
 * @date 2016/08/18.
 */
public enum JobType {
    ZK_JOB(1),//通过服务器端协调执行的任务，由jobserver通过zookeeper触发本地执行
    LOCAL(2),//在本地执行的任务(jobserver仍可提供监控，锁等)
    HTTP_JOB(3);//服务器通过soa-proxy方式调用的job
    private int type;

    JobType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static JobType valueOf(int type) {
        switch (type) {
            case 1:
                return ZK_JOB;
            case 2:
                return LOCAL;
            case 3:
                return HTTP_JOB;
        }
        return null;
    }
}
