package com.weimob.jobserver.core.job.commond;

/**
 * @author: kevin
 * @date 2016/08/14.
 */
public enum CommondType {
    PAUSE(1),
    RESUME(2),
    DELETE(3),
    TRIGGER(4);
    private int type;

    CommondType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static CommondType valueOf(int type) {
        switch (type) {
            case 4:
                return TRIGGER;
            case 2:
                return RESUME;
            case 1:
                return PAUSE;
            case 3:
                return DELETE;
        }
        return null;
    }

    public static int value(CommondType commondType) {
        return commondType.getType();
    }
}
