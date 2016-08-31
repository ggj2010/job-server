package com.weimob.jobserver.client.listener;

import com.weimob.jobserver.client.register.InitConstants;
import com.weimob.jobserver.core.job.BaseAbstractJob;
import com.weimob.jobserver.core.job.RemoteJob;
import com.weimob.jobserver.core.job.commond.ClientJobCommond;
import com.weimob.jobserver.core.job.dynamic.DynamicJob;
import com.weimob.jobserver.core.job.dynamic.DynamicJobManager;
import com.weimob.jobserver.core.job.listener.ZkDataChangeListener;
import com.weimob.jobserver.core.job.localstore.JobServiceStorage;
import com.weimob.jobserver.core.reg.domain.JobConfiguration;
import com.weimob.jobserver.core.reg.storage.JobNodePath;
import com.weimob.jobserver.core.zk.ZookeeperRegistryCenter;
import com.weimob.jobserver.core.zk.utils.ZkPathUtil;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author: kevin
 * @date 2016/08/19.
 */
public class ClientEventListener implements ZkDataChangeListener {
    private JobServiceStorage jobServiceStorage;
    private DynamicJobManager dynamicJobManager;
    private ZookeeperRegistryCenter zkCenter;

    public ClientEventListener(JobServiceStorage jobServiceStorage, DynamicJobManager dynamicJobManager, ZookeeperRegistryCenter zkCenter) {
        this.jobServiceStorage = jobServiceStorage;
        this.dynamicJobManager = dynamicJobManager;
        this.zkCenter = zkCenter;
    }


    private DynamicJob getDynamicJob(JobConfiguration jobConfig) {
        DynamicJob dynamicJob = new DynamicJob(jobConfig.getJobName());
        dynamicJob.jobGroup(jobConfig.getGroup());
        return dynamicJob;
    }


    @Override
    public void eventHandle(String data, String path) throws Exception {
        if (StringUtils.isEmpty(data)) {
            return;
        }
        if (!InitConstants.clientIp.equals(data)) {
            return;
        }
        List<String> childPathList = ZkPathUtil.parseZkPath(path);
        String systemId = childPathList.get(0);
        String jobName = childPathList.get(1);
        JobNodePath jobNodePath = new JobNodePath(systemId, jobName);
        try {
            BaseAbstractJob abstractJob = jobServiceStorage.get(jobName);
            if (abstractJob instanceof RemoteJob) {
                RemoteJob remoteJob = (RemoteJob) abstractJob;
                remoteJob.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            zkCenter.persistEphemeral(jobNodePath.getConfigNodePath(), ClientJobCommond.TRIGGERED.name());
        }
    }
}
