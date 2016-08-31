package com.weimob.jobserver.server.job.database.constant;

/**
 * @author: kevin
 * @date 2016/08/11.
 */
public enum ServerStatus {
    OK(1),//正常状态
    OFFILNE(2),//心跳检测失败，离线状态
    DELETED(3),//已经删除
    UNKOWN(-1),//错误状态

    ;
    private Integer status;

    ServerStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    public static ServerStatus valueOf(int status) {
        switch (status) {
            case 1:
                return OK;
            case 2:
                return OFFILNE;
            case 3:
                return DELETED;
        }
        return UNKOWN;
    }
}
