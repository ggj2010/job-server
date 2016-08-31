package com.weimob.jobserver.client.register;

import com.weimob.jobserver.core.job.BaseAbstractJob;
import com.weimob.jobserver.core.job.dynamic.DynamicJobManager;
import com.weimob.jobserver.core.job.localstore.JobServiceStorage;
import com.weimob.jobserver.core.job.reg.JobRegisterCenter;
import com.weimob.jobserver.core.redis.RedisClientUtil;
import com.weimob.jobserver.core.zk.ZookeeperConfiguration;
import com.weimob.jobserver.core.zk.ZookeeperRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.List;

/**
 * job管理
 *
 * @author: kevin
 * @date 2016/08/19.
 */
@Slf4j
public class JobManagerCenter {
    private RedisClientUtil redisClient;
    private SchedulerFactoryBean scheduler;
    private JobServiceStorage jobServiceStorage;
    private ThreadPoolTaskExecutor taskExecutor;
    private List<BaseAbstractJob> abstractJobs;
    private ZookeeperRegistryCenter zkCenter;
    private JobRegisterCenter jobRegisterCenter;
    private DynamicJobManager dynamicJobManager;
    public JobManagerCenter(RedisClientUtil redisClient, SchedulerFactoryBean scheduler, List<BaseAbstractJob> abstractJobs) {
        this.redisClient = redisClient;
        this.scheduler = scheduler;
        this.abstractJobs = abstractJobs;
    }

    public void init() throws Exception {
        if (abstractJobs == null || abstractJobs.size() <= 0) {
            log.warn("no job to register");
            return;
        }
        jobServiceStorage = new JobServiceStorage(abstractJobs);
        zkCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration(InitConstants.zookeeperAddress, InitConstants.DEFAULT_NAMESPACE));
        zkCenter.init();
        dynamicJobManager = new DynamicJobManager(scheduler.getScheduler());
        jobRegisterCenter = new ZookeeperJobRegisterCenter(zkCenter, jobServiceStorage, dynamicJobManager, redisClient);
        for (BaseAbstractJob abstractJob : abstractJobs) {
            jobRegisterCenter.register(abstractJob);
        }
    }

    public JobServiceStorage getJobServiceStorage() {
        return jobServiceStorage;
    }

    public ThreadPoolTaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public List<BaseAbstractJob> getAbstractJobs() {
        return abstractJobs;
    }

    public RedisClientUtil getRedisClient() {
        return redisClient;
    }

    public DynamicJobManager getDynamicJobManager() {
        return dynamicJobManager;
    }
}
