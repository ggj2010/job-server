package com.weimob.jobserver.core.job.localstore;

import com.weimob.jobserver.core.job.BaseAbstractJob;
import com.weimob.jobserver.core.reg.domain.JobConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: kevin
 * @date 2016/08/18.
 */
public class JobServiceStorage {
    private List<BaseAbstractJob> jobList;
    private Map<String, BaseAbstractJob> jobMap;

    public JobServiceStorage(List<BaseAbstractJob> jobList) {
        setJobList(jobList);
    }

    private void setJobList(List<BaseAbstractJob> jobList) {
        if (jobList == null || jobList.size() <= 0) {
            return;
        }
        jobMap = new HashMap<>(jobList.size());
        for (BaseAbstractJob baseAbstractJob : jobList) {
            JobConfiguration jobConfig = baseAbstractJob.getConfig();
            jobMap.put(jobConfig.getJobName(), baseAbstractJob);
        }
    }

    public BaseAbstractJob get(String jobKey) {
        if (jobMap == null) {
            return null;
        }
        return jobMap.get(jobKey);
    }
}
