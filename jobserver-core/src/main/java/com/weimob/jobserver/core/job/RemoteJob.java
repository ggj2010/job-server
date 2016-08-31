package com.weimob.jobserver.core.job;

import com.alibaba.fastjson.JSON;
import com.weimob.jobserver.core.job.log.JobLogInfo;
import com.weimob.jobserver.core.redis.RedisClientUtil;
import com.weimob.jobserver.core.reg.domain.RemoteJobConfig;
import com.weimob.jobserver.core.tools.CommonConstants;
import com.weimob.jobserver.core.zk.RegistryCenter;

/**
 * @author: kevin
 * @date 2016/08/18.
 */
public abstract class RemoteJob implements BaseAbstractJob<RemoteJobConfig> {
    private RemoteJobConfig config;
    private static RegistryCenter registryCenter;
    private static RedisClientUtil redisClient;

    @Override
    public void beforeExecute(RemoteJobConfig jobConfig) {

    }

    public void execute() {
        JobLogInfo jobLogInfo = new JobLogInfo(config.getSystemId(), config.getJobName(), config.getGroup(), System.currentTimeMillis(), config.getIp());
        try {
            beforeExecute(config);
            executeInternal(config);
            afterExecute(config);
            jobLogInfo.setSuccess(true);
        } catch (Exception e) {
            jobLogInfo.setSuccess(false);
        } finally {
            jobLogInfo.setEndTime(System.currentTimeMillis());
            redisClient.lpush(CommonConstants.LOG_REDIS_KEY, JSON.toJSONString(jobLogInfo));
        }
    }

    @Override
    public void afterExecute(RemoteJobConfig jobConfig) {

    }

    public void setConfig(RemoteJobConfig config) {
        this.config = config;
    }

    @Override
    public RemoteJobConfig getConfig() {
        return config;
    }

    public RegistryCenter getRegistryCenter() {
        return registryCenter;
    }

    public void setRegistryCenter(RegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
    }

    @Override
    public void setRedisClient(RedisClientUtil redisClient) {
        RemoteJob.redisClient = redisClient;
    }
}
