package com.weimob.jobserver.server.init;

import com.weimob.jobserver.core.job.BaseAbstractJob;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: kevin
 * @date 2016/08/19.
 */
public class JobCache {
    private static List<BaseAbstractJob> jobList = new ArrayList<>();

    public static void addJob(BaseAbstractJob baseAbstractJob) {
        jobList.add(baseAbstractJob);
    }

    public static List<BaseAbstractJob> getJobList() {
        return jobList;
    }
}
