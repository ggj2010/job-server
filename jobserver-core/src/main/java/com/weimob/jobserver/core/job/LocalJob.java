package com.weimob.jobserver.core.job;

import com.alibaba.fastjson.JSON;

import com.weimob.jobserver.core.job.localstore.JobServiceStorage;
import com.weimob.jobserver.core.job.log.JobLogInfo;
import com.weimob.jobserver.core.redis.RedisClientUtil;
import com.weimob.jobserver.core.reg.domain.LocalJobConfig;
import com.weimob.jobserver.core.tools.CommonConstants;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

/**
 * @author: kevin
 * @date 2016/08/18.
 */
public abstract class LocalJob implements BaseAbstractJob<LocalJobConfig>, Job {
    private LocalJobConfig config;
    private static JobServiceStorage jobServiceStorage;
    private static RedisClientUtil redisClient;

    @Override
    public abstract void beforeExecute(LocalJobConfig jobConfig);

    @Override
    public abstract void afterExecute(LocalJobConfig jobConfig);


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobKey jobKey = jobExecutionContext.getJobDetail().getKey();
        BaseAbstractJob baseAbstractJob = jobServiceStorage.get(jobKey.getName());
        LocalJobConfig jobConfiguration = (LocalJobConfig) baseAbstractJob.getConfig();
        JobLogInfo jobLogInfo = new JobLogInfo(jobConfiguration.getSystemId(), jobConfiguration.getJobName(), jobConfiguration.getGroup(), System.currentTimeMillis(), jobConfiguration.getIp());
        try {
            beforeExecute(jobConfiguration);
            executeInternal(jobConfiguration);
            jobLogInfo.setSuccess(true);
            jobLogInfo.setEndTime(System.currentTimeMillis());
            afterExecute(jobConfiguration);
        } catch (Exception e) {
            jobLogInfo.setSuccess(false);
            throw new JobExecutionException(e);
        } finally {
            redisClient.lpush(CommonConstants.LOG_REDIS_KEY, JSON.toJSONString(jobLogInfo));
        }
    }

    @Override
    public LocalJobConfig getConfig() {
        return config;
    }

    public void setConfig(LocalJobConfig config) {
        this.config = config;
    }

    public void setJobServiceStorage(JobServiceStorage jobServiceStorage) {
        LocalJob.jobServiceStorage = jobServiceStorage;
    }

    @Override
    public void setRedisClient(RedisClientUtil redisClient) {
        LocalJob.redisClient = redisClient;
    }
}
