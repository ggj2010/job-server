package com.weimob.jobserver.core.reg.constant;

/**
 * 任务锁策略
 *
 * @author: kevin
 * @date 2016/08/02.
 */
public enum JobLockType {
    DEFAULT(1),//默认策略，不会使用锁
    OVVERAL_UNIQUE(2);//全局唯一，将会使用锁机制保证任务不会重复运行
    private Integer type;

    JobLockType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public static JobLockType valueOf(int type) {
        switch (type) {
            case 1:
                return DEFAULT;
            case 2:
                return OVVERAL_UNIQUE;
        }
        return null;
    }
}