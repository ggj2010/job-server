package com.weimob.jobserver.server.job.handler;

import com.alibaba.fastjson.JSON;
import com.weimob.jobserver.core.job.dynamic.DynamicJob;
import com.weimob.jobserver.core.job.dynamic.DynamicJobManager;
import com.weimob.jobserver.core.reg.constant.JobStatus;
import com.weimob.jobserver.core.reg.domain.JobConfiguration;
import com.weimob.jobserver.server.event.data.JobEventType;
import com.weimob.jobserver.server.event.handler.BaseEventHandler;
import com.weimob.jobserver.server.job.database.baen.JobConfigEntity;
import com.weimob.jobserver.server.job.database.service.JobConfigService;
import com.weimob.jobserver.server.job.database.service.ServerConfigEntityService;
import com.weimob.jobserver.server.job.util.JobUtil;
import lombok.extern.log4j.Log4j;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: kevin
 * @date 2016/08/20.
 */
@Log4j
@Component
public class JobDataHandler implements BaseEventHandler<JobConfiguration> {
    @Autowired
    private DynamicJobManager jobManager;

    @Autowired
    private JobConfigService jobConfigService;
    @Autowired
    private ServerConfigEntityService serverConfigEntityService;

    @Override
    public JobEventType getEventType() {
        return JobEventType.ADD_UPDATE_JOB;
    }

    @Override
    public void onChange(JobConfiguration jobConfig) {
        try {
            JobConfigEntity job = new JobConfigEntity(jobConfig.getSystemId(), jobConfig.getJobClass(), JobStatus.EXECUTING, jobConfig.getJobType().getType(), jobConfig.getJobName(), jobConfig.getCron(), jobConfig.getGroup(), jobConfig.getLockType().getType());
            JobConfigEntity persistEntity = jobConfigService.get(job);
            Integer jobId;
            if (persistEntity == null) {
                jobConfigService.save(job);
                jobId = job.getId();
            } else {
                jobId = persistEntity.getId();
            }
            log.info("ADD_JOB :" + JSON.toJSONString(jobConfig));
            JobStatus jobStatus = jobConfig.getJobStatus();
            switch (jobConfig.getJobType()) {
                case HTTP_JOB:
                case ZK_JOB:
                    switch (jobStatus) {
                        case EXECUTING:
                            DynamicJob dynamicJob = JobUtil.getDynamicJob(jobConfig, jobId);
                            log.info("createAndRegisterJob:" + jobConfig.getJobName());
                            jobManager.createAndRegisterJob(dynamicJob, true);
                            break;
                        case PAUSED:
                            String jobName = job.getSystemId() + "_" + job.getJobName();
                            jobManager.pauseJob(new JobKey(jobName, job.getGroupName()));
                            break;
                        case DELETED:
                            DynamicJob delDynamicJob = JobUtil.getDynamicJob(jobConfig, jobId);
                            jobManager.deleteJob(delDynamicJob);
                            break;
                    }
                    break;
                case LOCAL:
                    break;
            }
            if (persistEntity != null) {
                if (!persistEntity.isSame(job)) {
                    updateJob(persistEntity, jobConfig);
                }
            }
        } catch (Exception e) {
            log.error("onChange error data:" + JSON.toJSONString(jobConfig), e);
        }
    }

    private void updateJob(JobConfigEntity persistEntity, JobConfiguration jobConfig) {
        persistEntity.setCronExpression(jobConfig.getCron());
        persistEntity.setJobClass(jobConfig.getJobClass());
        persistEntity.setStatus(jobConfig.getJobStatus().getStatus());
        persistEntity.setParameterTypes(jobConfig.getParameterTypes());
        persistEntity.setJobType(jobConfig.getJobType().getType());
        jobConfigService.update(persistEntity);
    }
}
