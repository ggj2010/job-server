package com.weimob.jobserver.client.register;

import com.alibaba.fastjson.JSON;
import com.weimob.jobserver.client.listener.ClientEventListener;
import com.weimob.jobserver.client.listener.JobConfigEventListener;
import com.weimob.jobserver.core.job.BaseAbstractJob;
import com.weimob.jobserver.core.job.LocalJob;
import com.weimob.jobserver.core.job.RemoteJob;
import com.weimob.jobserver.core.job.commond.ClientJobCommond;
import com.weimob.jobserver.core.job.dynamic.DynamicJob;
import com.weimob.jobserver.core.job.dynamic.DynamicJobManager;
import com.weimob.jobserver.core.job.localstore.JobServiceStorage;
import com.weimob.jobserver.core.job.reg.JobRegisterCenter;
import com.weimob.jobserver.core.redis.RedisClientUtil;
import com.weimob.jobserver.core.reg.domain.JobConfiguration;
import com.weimob.jobserver.core.reg.exception.RegException;
import com.weimob.jobserver.core.reg.storage.JobNodePath;
import com.weimob.jobserver.core.zk.ZookeeperRegistryCenter;
import lombok.extern.log4j.Log4j;
import org.apache.log4j.Logger;
import org.quartz.JobKey;
import org.springframework.util.StringUtils;

/**
 * @author: kevin
 * @date 2016/08/19.
 */
@Log4j
public class ZookeeperJobRegisterCenter implements JobRegisterCenter<BaseAbstractJob> {
    private Logger logger = Logger.getLogger(ZookeeperJobRegisterCenter.class);
    private ZookeeperRegistryCenter zkCenter;
    private JobServiceStorage jobServiceStorage;
    private DynamicJobManager dynamicJobManager;
    private RedisClientUtil redisClient;

    public ZookeeperJobRegisterCenter(ZookeeperRegistryCenter zkCenter, JobServiceStorage jobServiceStorage, DynamicJobManager dynamicJobManager) {
        this.zkCenter = zkCenter;
        this.jobServiceStorage = jobServiceStorage;
        this.dynamicJobManager = dynamicJobManager;
    }

    public ZookeeperJobRegisterCenter(ZookeeperRegistryCenter zkCenter, JobServiceStorage jobServiceStorage, DynamicJobManager dynamicJobManager, RedisClientUtil redisClient) {
        this.zkCenter = zkCenter;
        this.jobServiceStorage = jobServiceStorage;
        this.dynamicJobManager = dynamicJobManager;
        this.redisClient = redisClient;
    }

    @Override
    public void register(BaseAbstractJob job) throws Exception {
        JobNodePath jobNodePath;
        JobConfiguration jobConfig = job.getConfig();
        if (StringUtils.isEmpty(jobConfig.getSystemId())) {
            throw new Exception("jobserver systemId cannot be null,check your config file");
        }
        jobNodePath = new JobNodePath(jobConfig.getSystemId(), jobConfig.getJobName());
        String jobPath = jobNodePath.getJobDataPath();
        job.setRedisClient(redisClient);
        String serverPath = jobNodePath.getServerNodePath(jobConfig.getIp());
        String jobConfigPath = jobNodePath.getConfigNodePath();
        ClientJobCommond clientJobCommond = null;
        if (zkCenter.isExisted(jobPath)) {//job节点持久缓存
            try {
                String commondStr = zkCenter.get(jobConfigPath);
                if (!StringUtils.isEmpty(commondStr)) {
                    clientJobCommond = ClientJobCommond.valueOf(commondStr);
                }
            } catch (IllegalArgumentException e) {
                zkCenter.remove(jobConfigPath);
                logger.error("get commond error,data:" + JSON.toJSONString(jobConfigPath), e);
                throw new RegException(e);
            }
            if (clientJobCommond != null && clientJobCommond == ClientJobCommond.DELETE) {//删除状态则不再注册
                return;
            }
        }
        zkCenter.persist(jobNodePath.getJobDataPath(), JSON.toJSONString(jobConfig));
        zkCenter.persistEphemeral(serverPath, "");//服务器节点，临时缓存，服务器下线自动删除
        zkCenter.addNodeCacheWatcher(jobConfigPath, new JobConfigEventListener(jobServiceStorage, dynamicJobManager, zkCenter));//监控jobConfig节点
        switch (job.getConfig().getJobType()) {
            case LOCAL:
                LocalJob localJob = (LocalJob) job;
                localJob.setJobServiceStorage(jobServiceStorage);
                DynamicJob dynamicJob = new DynamicJob(jobConfig.getJobName());
                dynamicJob.jobGroup(jobConfig.getGroup());
                dynamicJob.cronExpression(jobConfig.getCron());
                dynamicJob.target(localJob.getClass());
                dynamicJobManager.createAndRegisterJob(dynamicJob, true);
                if (clientJobCommond != null && clientJobCommond == ClientJobCommond.PAUSE) {
                    dynamicJobManager.pauseJob(new JobKey(jobConfig.getSystemId() + "_" + jobConfig.getJobName()));
                }
                return;
        }
        ((RemoteJob) job).setRegistryCenter(zkCenter);
        zkCenter.addNodeCacheWatcher(jobNodePath.getExecutionNodePath(), new ClientEventListener(jobServiceStorage, dynamicJobManager, zkCenter));//监控服务器commond节点
    }

}
