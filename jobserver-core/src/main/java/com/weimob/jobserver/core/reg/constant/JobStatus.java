package com.weimob.jobserver.core.reg.constant;

/**
 * @author: kevin
 * @date 2016/08/04.
 */
public enum JobStatus {
    EXECUTING(1),//正在执行
    PAUSED(2),//暂停执行(暂停所有，直到出发继续执行)
    ERROR(3),//执行出错
    DELETED(4),//已经删除
    ;

    private int status;

    JobStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public static JobStatus valueOf(int status) {
        switch (status) {
            case 1:
                return EXECUTING;
            case 2:
                return PAUSED;
            case 3:
                return ERROR;
        }
        return null;
    }
}
