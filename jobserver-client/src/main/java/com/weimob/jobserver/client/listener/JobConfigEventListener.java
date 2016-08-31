package com.weimob.jobserver.client.listener;

import com.weimob.jobserver.core.job.BaseAbstractJob;
import com.weimob.jobserver.core.job.commond.ClientJobCommond;
import com.weimob.jobserver.core.job.dynamic.DynamicJobManager;
import com.weimob.jobserver.core.job.listener.ZkDataChangeListener;
import com.weimob.jobserver.core.job.localstore.JobServiceStorage;
import com.weimob.jobserver.core.reg.domain.JobConfiguration;
import com.weimob.jobserver.core.reg.storage.JobNodePath;
import com.weimob.jobserver.core.zk.ZookeeperRegistryCenter;
import com.weimob.jobserver.core.zk.utils.ZkPathUtil;
import org.apache.commons.lang.StringUtils;
import org.quartz.JobKey;

import java.util.List;

/**
 * @author: kevin
 * @date 2016/08/21.
 */
public class JobConfigEventListener implements ZkDataChangeListener {
    private JobServiceStorage jobServiceStorage;
    private DynamicJobManager dynamicJobManager;
    private ZookeeperRegistryCenter zkCenter;

    public JobConfigEventListener(JobServiceStorage jobServiceStorage, DynamicJobManager dynamicJobManager, ZookeeperRegistryCenter zkCenter) {
        this.jobServiceStorage = jobServiceStorage;
        this.dynamicJobManager = dynamicJobManager;
        this.zkCenter = zkCenter;
    }

    @Override
    public void eventHandle(String dataJson, String path) throws Exception {
        if (StringUtils.isEmpty(dataJson)) {
            return;
        }
        List<String> childPathList = ZkPathUtil.parseZkPath(path);
        String systemId = childPathList.get(0);
        String jobName = childPathList.get(1);
        JobNodePath jobNodePath = new JobNodePath(systemId, jobName);
        ClientJobCommond clientJobCommond = ClientJobCommond.valueOf(zkCenter.get(jobNodePath.getConfigNodePath()));
        BaseAbstractJob abstractJob = jobServiceStorage.get(jobName);
        JobConfiguration jobConfig = abstractJob.getConfig();
        boolean needRefreshTrigger = true;
        switch (jobConfig.getJobType()) {//zk和http类型不需要处理本地
            case HTTP_JOB:
                return;
            case ZK_JOB:
                needRefreshTrigger = false;
                break;
        }
        switch (clientJobCommond) {
            case PAUSE:
                if (needRefreshTrigger) {
                    dynamicJobManager.pauseJob(new JobKey(jobName, jobConfig.getGroup()));
                }
                zkCenter.persistEphemeral(jobNodePath.getConfigNodePath(), ClientJobCommond.PAUSED.name());
                break;
            case DELETED:
                if (needRefreshTrigger) {
                    dynamicJobManager.resumeJob(new JobKey(jobName, jobConfig.getGroup()));
                }
                zkCenter.persistEphemeral(jobNodePath.getConfigNodePath(), ClientJobCommond.DELETED.name());
                break;
            case RESUME:
                if (needRefreshTrigger) {
                    dynamicJobManager.resumeJob(new JobKey(jobName, jobConfig.getGroup()));
                }
        }
    }
}
