package com.weimob.jobserver.server.monitor.constant;

/**
 * @author: kevin
 * @date 2016/08/13.
 */
public enum MonitorErrorCode {
    SUCESS(0, "操作成功"),
    SERVER_ERROR(500, "服务器错误"),
    HEARBEAT_CHECK_FAIIL(101, "心跳检测失败"),
    NO_AVAILABLE_SERVER(102, "没有服务器"),//
    SERVER_NOT_EXISTS(103, "服务器不存在或已经下线"),//
    JOB_NOT_EXISTS(201, "job不存在"),//
    PAUSE_FAIL(202, "暂停失败"),//
    RESUME_FAIL(203, "重启失败"),//
    JOB_NAME_EMPTY(301, "任务名称不能为空"),//
    CLIENT_PATH_INVALID(302, "无效的系统访问路径或者端口"),//
    CLIENT_NAME_EMPTY(303, "clientName不能为空"),//
    INVALID_PORT(304, "无效的端口号"),//
    CLIENT_NOT_AVALIABLE(305, "无效的客户端"),//手动添加任务时，需要填写客户端信息，如果校验失败，返回此值
    ;
    private Integer errorCode;
    private String msg;

    MonitorErrorCode(Integer errorCode, String msg) {
        this.errorCode = errorCode;
        this.msg = msg;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getMsg() {
        return msg;
    }
}
