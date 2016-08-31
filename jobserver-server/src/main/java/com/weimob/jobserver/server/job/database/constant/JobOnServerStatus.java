package com.weimob.jobserver.server.job.database.constant;

/**
 * 某个任务在特定服务器上的运行状态
 *
 * @author: kevin
 * @date 2016/08/11.
 */
public enum JobOnServerStatus {
    RUNNING(1),//正在执行
    DELETED(2),//删除
    ;
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    JobOnServerStatus(Integer status) {
        this.status = status;
    }
}
