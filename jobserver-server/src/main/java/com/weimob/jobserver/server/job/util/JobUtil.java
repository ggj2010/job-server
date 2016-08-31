package com.weimob.jobserver.server.job.util;

import com.weimob.jobserver.core.job.dynamic.DynamicJob;
import com.weimob.jobserver.core.reg.constant.JobType;
import com.weimob.jobserver.core.reg.domain.JobConfiguration;
import com.weimob.jobserver.server.job.database.baen.JobConfigEntity;
import com.weimob.jobserver.server.job.executor.HttpJobExecutor;
import com.weimob.jobserver.server.job.executor.RemoteJobExecutor;
import org.quartz.Job;

/**
 * @author: kevin
 * @date 2016/08/22.
 */
public class JobUtil {
    public static DynamicJob getDynamicJob(JobConfiguration jobConfig, Integer jobId) {
        boolean isRegister = false;
        DynamicJob dynamicJob = null;
        Class<? extends Job> targetClass = null;
        switch (jobConfig.getJobType()) {
            case HTTP_JOB:
                isRegister = true;
                targetClass = HttpJobExecutor.class;
                break;
            case ZK_JOB:
                isRegister = true;
                targetClass = RemoteJobExecutor.class;
                break;
        }
        if (isRegister) {
            dynamicJob = new DynamicJob(jobConfig.getSystemId() + "_" + jobConfig.getJobName());
            dynamicJob.jobGroup(jobConfig.getGroup());
            dynamicJob.cronExpression(jobConfig.getCron());
            dynamicJob.target(targetClass);
            dynamicJob.addJobData("jobId", jobId);
        }
        return dynamicJob;
    }

    public static DynamicJob getDynamicJob(JobConfigEntity jobConfig) {
        DynamicJob dynamicJob = new DynamicJob(jobConfig.getSystemId() + "_" + jobConfig.getJobName());
        dynamicJob.jobGroup(jobConfig.getGroupName());
        JobType jobType = JobType.valueOf(jobConfig.getJobType());
        switch (jobType) {
            case HTTP_JOB:
                dynamicJob.target(HttpJobExecutor.class);
                break;
            case ZK_JOB:
                dynamicJob.target(RemoteJobExecutor.class);
                break;
            case LOCAL:
                return null;
        }
        dynamicJob.cronExpression(jobConfig.getCronExpression());
        dynamicJob.addJobData("jobId", jobConfig.getId());
        return dynamicJob;
    }
}
