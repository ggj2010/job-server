package com.weimob.jobserver.example;

import com.weimob.jobserver.core.job.RemoteJob;
import com.weimob.jobserver.core.reg.annotation.ScheduledJob;
import com.weimob.jobserver.core.reg.domain.RemoteJobConfig;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: kevin
 * @date 2016/08/03.
 */
@ScheduledJob( jobName = "remoteTest2", cronExp = "0/15 * * * * ?")
public class RemoteJobTest2 extends RemoteJob {
    private static Long lastTime = System.currentTimeMillis();

    @Override
    public void executeInternal(RemoteJobConfig jobConfig) {
        String date = new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date());
        System.out.println("RemoteJobTest2 execute at:" + date + "   " + ((System.currentTimeMillis() - lastTime) / 1000));
    }
}
