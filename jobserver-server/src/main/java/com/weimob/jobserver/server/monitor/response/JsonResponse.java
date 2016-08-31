package com.weimob.jobserver.server.monitor.response;

import com.weimob.jobserver.server.monitor.constant.MonitorErrorCode;

import java.io.Serializable;

/**
 * @author: kevin
 * @date 2016/08/13.
 */
public class JsonResponse<T> implements Serializable {
    private Integer errorCode;
    private Boolean success;
    private String msg;
    private T data;

    public JsonResponse() {
    }

    public JsonResponse(MonitorErrorCode errorCode) {
        this.errorCode = errorCode.getErrorCode();
        this.msg = errorCode.getMsg();
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
