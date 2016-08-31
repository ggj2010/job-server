package com.weimob.jobserver.server.zk.listener;

import com.alibaba.fastjson.JSONObject;
import com.weimob.jobserver.core.job.listener.ZkDataChangeListener;
import com.weimob.jobserver.core.reg.constant.JobStatus;
import com.weimob.jobserver.core.reg.domain.RemoteJobConfig;
import com.weimob.jobserver.core.zk.ZookeeperRegistryCenter;
import com.weimob.jobserver.core.zk.utils.ZkPathUtil;
import com.weimob.jobserver.server.event.EventProducer;
import com.weimob.jobserver.server.event.data.JobEventType;
import com.weimob.jobserver.server.event.data.JobServerEvent;
import com.weimob.jobserver.server.job.database.baen.JobConfigEntity;
import com.weimob.jobserver.server.job.database.service.JobConfigService;
import com.weimob.jobserver.server.utils.SpringContextHolder;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author: kevin
 * @date 2016/08/23.
 */
public class JobDataListener implements ZkDataChangeListener {
    private ZookeeperRegistryCenter zkCenter;
    private EventProducer eventProducer;
    private JobConfigService jobConfigService;

    public JobDataListener() {
        this.zkCenter = SpringContextHolder.getBean(ZookeeperRegistryCenter.class);
        this.eventProducer = SpringContextHolder.getBean(EventProducer.class);
        this.jobConfigService = SpringContextHolder.getBean(JobConfigService.class);
    }

    @Override
    public void eventHandle(String dataJson, String path) throws Exception {
        if (StringUtils.isEmpty(dataJson)) {
            return;
        }
        List<String> nodePathList = ZkPathUtil.parseZkPath(path);
        String systemId = nodePathList.get(0);
        String jobName = nodePathList.get(1);
        JobConfigEntity jobConfigEntity = new JobConfigEntity(jobName, systemId);
        String jobDataJson = zkCenter.get(path);
        if (StringUtils.isEmpty(jobDataJson)) {
            return;
        }
        RemoteJobConfig jobConfig = JSONObject.parseObject(jobDataJson, RemoteJobConfig.class);
        jobConfigEntity = new JobConfigEntity(systemId, jobConfig.getJobClass(), JobStatus.EXECUTING, jobConfig.getJobType().getType(), jobName, jobConfig.getCron(), jobConfig.getGroup(), jobConfigEntity.getLockStratergy());
        eventProducer.sendMsg(new JobServerEvent(jobConfigEntity, JobEventType.ADD_UPDATE_JOB));
    }
}
