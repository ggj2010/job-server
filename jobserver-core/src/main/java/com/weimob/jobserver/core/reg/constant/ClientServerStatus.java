package com.weimob.jobserver.core.reg.constant;

/**
 * @author: kevin
 * @date 2016/08/11.
 */
public enum ClientServerStatus {
    TRIGGER(1),//正常状态
    RESUME(2),//已经删除
    PAUSE(3),//暂停运行
    UNKOWN(-1),//错误状态
    ;
    private Integer status;

    ClientServerStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    public static ClientServerStatus valueOf(int status) {
        switch (status) {
            case 1:
                return TRIGGER;
            case 2:
                return RESUME;
            case 3:
                return PAUSE;
        }
        return UNKOWN;
    }
}
