package com.weimob.jobserver.server.init;


import com.weimob.jobserver.core.job.dynamic.DynamicJob;
import com.weimob.jobserver.core.job.dynamic.DynamicJobManager;
import com.weimob.jobserver.core.reg.constant.JobStatus;
import com.weimob.jobserver.core.zk.ZookeeperRegistryCenter;
import com.weimob.jobserver.server.job.database.baen.JobConfigEntity;
import com.weimob.jobserver.server.job.database.service.JobConfigService;
import com.weimob.jobserver.server.job.util.JobUtil;
import com.weimob.jobserver.server.zk.listener.SystemListener;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: kevin
 * @date 2016/08/03.
 */

@Log4j
@Component
public class ServerJobInitializer {
    @Autowired
    private ZookeeperRegistryCenter registryCenter;
    @Autowired
    private JobConfigService jobConfigService;
    @Autowired
    private DynamicJobManager jobManager;

    private static SystemListener systemListener;

    private static AtomicBoolean inited = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        if (inited.compareAndSet(false, true)) {
            initJob();
        }
    }

    private void initJob() {
        systemListener = new SystemListener();
        try {
            registryCenter.addChildWatcher("/", systemListener);
            JobConfigEntity configEntity = new JobConfigEntity();
            List<JobConfigEntity> jobConfigEntities = jobConfigService.loadInitJobs(configEntity);
            if (!CollectionUtils.isEmpty(jobConfigEntities)) {
                for (JobConfigEntity jobConfigEntity : jobConfigEntities) {
                    JobStatus jobStatus = JobStatus.valueOf(jobConfigEntity.getStatus());
                    if (jobStatus == JobStatus.DELETED) {
                        return;
                    }
                    DynamicJob dynamicJob = JobUtil.getDynamicJob(jobConfigEntity);
                    switch (jobStatus) {
                        case PAUSED:
                            jobManager.createAndRegisterJob(dynamicJob, true);
                            jobManager.pauseJob(dynamicJob.jobDetail().getKey());
                            break;
                        case EXECUTING:
                        case ERROR:
                            jobManager.createAndRegisterJob(dynamicJob, true);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("init error", e);
        }
    }
}
