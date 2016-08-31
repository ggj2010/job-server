package com.weimob.jobserver.server.zk.listener;

import com.alibaba.fastjson.JSONObject;
import com.weimob.jobserver.core.job.commond.ClientJobCommond;
import com.weimob.jobserver.core.job.listener.ZkDataChangeListener;
import com.weimob.jobserver.core.reg.constant.JobStatus;
import com.weimob.jobserver.core.reg.domain.RemoteJobConfig;
import com.weimob.jobserver.core.reg.storage.JobNodePath;
import com.weimob.jobserver.core.zk.ZookeeperRegistryCenter;
import com.weimob.jobserver.core.zk.utils.ZkPathUtil;
import com.weimob.jobserver.server.event.EventProducer;
import com.weimob.jobserver.server.event.data.JobEventType;
import com.weimob.jobserver.server.event.data.JobServerEvent;
import com.weimob.jobserver.server.job.database.service.JobConfigService;
import com.weimob.jobserver.server.utils.SpringContextHolder;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author: kevin
 * @date 2016/08/22.
 */
@Log4j
public class JobConfigListener implements ZkDataChangeListener {
    private ZookeeperRegistryCenter zkCenter;
    private EventProducer eventProducer;
    private JobConfigService jobConfigService;

    public JobConfigListener() {
        this.zkCenter = SpringContextHolder.getBean(ZookeeperRegistryCenter.class);
        this.eventProducer = SpringContextHolder.getBean(EventProducer.class);
        this.jobConfigService = SpringContextHolder.getBean(JobConfigService.class);
    }

    @Override
    public void eventHandle(String dataStr, String path) throws Exception {
        if (StringUtils.isEmpty(dataStr)) {
            return;
        }
        ClientJobCommond clientJobCommond = ClientJobCommond.valueOf(dataStr);
        if (clientJobCommond == null) {
            return;
        }
        List<String> nodePathList = ZkPathUtil.parseZkPath(path);
        String systemId = nodePathList.get(0);
        String jobName = nodePathList.get(1);
        JobNodePath jobNodePath = new JobNodePath(systemId, jobName);
        String jobConfigJson = zkCenter.get(jobNodePath.getJobDataPath());
        log.info(String.format("jobConfigJson : %s", jobConfigJson));
        RemoteJobConfig jobConfig = JSONObject.parseObject(jobConfigJson, RemoteJobConfig.class);
        switch (clientJobCommond) {
            case PAUSED:
                jobConfig.setJobStatus(JobStatus.PAUSED);
                eventProducer.sendMsg(new JobServerEvent(jobConfig, JobEventType.ADD_UPDATE_JOB));
                break;
            case RESUMED:
                jobConfig.setJobStatus(JobStatus.EXECUTING);
                eventProducer.sendMsg(new JobServerEvent(jobConfig, JobEventType.ADD_UPDATE_JOB));
                break;
            case DELETED:
                jobConfig.setJobStatus(JobStatus.DELETED);
                eventProducer.sendMsg(new JobServerEvent(jobConfig, JobEventType.ADD_UPDATE_JOB));
                break;
            case TRIGGERED:
                break;
        }
    }
}
