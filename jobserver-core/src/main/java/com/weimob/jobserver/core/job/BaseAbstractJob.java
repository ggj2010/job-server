package com.weimob.jobserver.core.job;

import com.weimob.jobserver.core.redis.RedisClientUtil;
import com.weimob.jobserver.core.reg.domain.JobConfiguration;

/**
 * @author: kevin
 * @date 2016/08/18.
 */
public interface BaseAbstractJob<T extends JobConfiguration> {
    void beforeExecute(T jobConfig);

    void afterExecute(T jobConfig);

    T getConfig();

    void executeInternal(T jobConfig);

    void setRedisClient(RedisClientUtil redisClient);
}
