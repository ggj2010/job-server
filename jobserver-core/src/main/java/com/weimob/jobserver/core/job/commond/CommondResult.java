package com.weimob.jobserver.core.job.commond;


import com.weimob.jobserver.core.tools.DateUtil;

import java.io.Serializable;

/**
 * 远程服务器执行命令结果
 *
 * @author: kevin
 * @date 2016/08/14.
 */
public class CommondResult implements Serializable {
    private boolean success;
    private String timeStamp;

    public CommondResult() {
    }

    public CommondResult(boolean success) {
        this.success = success;
        timeStamp = DateUtil.currentDateTime();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
