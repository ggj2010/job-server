package com.weimob.jobserver.example;

import com.weimob.jobserver.core.job.LocalJob;
import com.weimob.jobserver.core.reg.annotation.ScheduledJob;
import com.weimob.jobserver.core.reg.domain.LocalJobConfig;

/**
 * @author: kevin
 * @date 2016/07/25.
 */
@ScheduledJob(jobName = "testJob", cronExp = "0/12 * * * * ?")
public class LocalLocalJobTest extends LocalJob {
    public LocalLocalJobTest() {
        System.out.println("TestJob inited");
    }

    @Override
    public void beforeExecute(LocalJobConfig jobConfig) {

    }

    @Override
    public void afterExecute(LocalJobConfig jobConfig) {

    }

    @Override
    public void executeInternal(LocalJobConfig jobConfig) {
        System.out.println("LocalLocalJobTest executed");
    }
}
