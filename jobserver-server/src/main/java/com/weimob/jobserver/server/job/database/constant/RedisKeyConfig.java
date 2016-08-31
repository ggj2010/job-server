package com.weimob.jobserver.server.job.database.constant;

/**
 * @author: kevin
 * @date 2016/08/11.
 */
public class RedisKeyConfig {
    public static String JOB_KEY = "job_{ID}";//job 根据id或者jobName缓存
    public static String JOB_SERVERS = "job_servers_{ID}";//job的服务器列表
}
