package com.weimob.jobserver.core.job.commond;

/**
 * job执行命令
 *
 * @author: kevin
 * @date 2016/08/23.
 */
public enum ClientJobCommond {
    PAUSE,//服务端触发的命令
    PAUSED,//client执行结果反馈
    RESUME,
    RESUMED,
    DELETE,
    DELETED,
    TRIGGERED
}
