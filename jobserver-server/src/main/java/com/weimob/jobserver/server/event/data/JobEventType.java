package com.weimob.jobserver.server.event.data;

/**
 * @author: kevin
 * @date 2016/08/06.
 */
public enum JobEventType {
    REGISTER,//注册任务
    ADD_UPDATE_JOB,//添加或更新任务，添加完成之后会注册
    DEL_JOB,//删除任务
    RESUME_JOB,//重新开始任务
    TRIGGER_JOB,//执行任务
    PAUSE_JOB,//中断任务
    CLIENT_ADD,//服务端事件通知
    CLIENT_REMOVE,//服务端下线
    CLIENT_ERROR,//服务端执行出错
    DELETE_SYSTEM,//删除接入系统
}
