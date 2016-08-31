package com.weimob.jobserver.server.job.executor;

import com.alibaba.fastjson.JSON;
import com.weimob.jobserver.core.reg.storage.JobNodePath;
import com.weimob.jobserver.core.zk.ZookeeperRegistryCenter;
import com.weimob.jobserver.server.job.database.baen.JobConfigEntity;
import com.weimob.jobserver.server.job.database.baen.ServerConfigEntity;
import com.weimob.jobserver.server.job.manager.JobManager;
import com.weimob.jobserver.server.utils.SpringContextHolder;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;

/**
 * @author: kevin
 * @date 2016/08/03.
 */
@Log4j
public class RemoteJobExecutor extends BaseRemoteJobExecutor {
    private static ZookeeperRegistryCenter registryCenter;

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Integer jobId = (Integer) jobExecutionContext.getMergedJobDataMap().get("jobId");
        JobManager jobManager = SpringContextHolder.getBean(JobManager.class);
        JobConfigEntity jobConfigEntity = jobManager.getWorkingServers(jobId);
        List<ServerConfigEntity> serverConfigEntities = jobConfigEntity.getServerConfigList();
        if (CollectionUtils.isEmpty(serverConfigEntities)) {
            log.warn(String.format("job [%s] jobName[%s] has no available server", jobId, jobConfigEntity.getJobName()));
            return;
        }
        final JobNodePath jobNodePath = new JobNodePath(jobConfigEntity.getSystemId(), jobConfigEntity.getJobName());
        ThreadPoolTaskExecutor taskExecutor = SpringContextHolder.getBean(ThreadPoolTaskExecutor.class);
        final ZookeeperRegistryCenter registryCenter = getRegistryCenter();
        for (final ServerConfigEntity configEntity : serverConfigEntities) {
            try {
                taskExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        String triggerNode = jobNodePath.getExecutionNodePath();
                        registryCenter.persistEphemeral(triggerNode, configEntity.getServerIp());
                    }
                });
            } catch (Exception e) {
                log.error("trigger job error,param:" + JSON.toJSONString(configEntity), e);
            }
        }
    }

    private ZookeeperRegistryCenter getRegistryCenter() {
        if (registryCenter == null) {
            registryCenter = SpringContextHolder.getBean(ZookeeperRegistryCenter.class);
        }
        return registryCenter;
    }
}
