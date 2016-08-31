package com.weimob.jobserver.server.job.handler;

import com.weimob.jobserver.core.reg.constant.JobStatus;
import com.weimob.jobserver.core.reg.domain.JobConfiguration;
import com.weimob.jobserver.server.event.data.JobEventType;
import com.weimob.jobserver.server.event.handler.BaseEventHandler;
import com.weimob.jobserver.server.job.database.baen.JobConfigEntity;
import com.weimob.jobserver.server.job.database.baen.ServerConfigEntity;
import com.weimob.jobserver.server.job.database.service.JobConfigService;
import com.weimob.jobserver.server.job.database.service.ServerConfigEntityService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author: kevin
 * @date 2016/08/22.
 */
@Log4j
@Component
public class ClientRemoveEventHandler implements BaseEventHandler<JobConfiguration> {
    @Autowired
    private JobConfigService jobConfigService;
    @Autowired
    private ServerConfigEntityService serverConfigEntityService;

    @Override
    public JobEventType getEventType() {
        return JobEventType.CLIENT_REMOVE;
    }

    @Override
    public void onChange(JobConfiguration jobConfig) {
        JobConfigEntity job = new JobConfigEntity(jobConfig.getSystemId(), jobConfig.getJobClass(), JobStatus.EXECUTING, jobConfig.getJobType().getType(), jobConfig.getJobName(), jobConfig.getCron(), jobConfig.getGroup(), jobConfig.getLockType().getType());
        JobConfigEntity persistEntity = jobConfigService.get(job);
        if (persistEntity == null) {
            return;
        }
        ServerConfigEntity param = new ServerConfigEntity(persistEntity.getId());
        Integer count = serverConfigEntityService.countServers(persistEntity.getId());
        List<ServerConfigEntity> serverJobConfigList = null;
        if (count != null && count == 1) {
            serverJobConfigList = serverConfigEntityService.findList(param);
        } else {
            param.setServerIp(jobConfig.getIp());
            serverJobConfigList = serverConfigEntityService.findList(param);
        }
        if (!CollectionUtils.isEmpty(serverJobConfigList)) {
            for (ServerConfigEntity serverConfigEntity : serverJobConfigList) {
                serverConfigEntityService.delete(serverConfigEntity);
            }
        }

    }

}
