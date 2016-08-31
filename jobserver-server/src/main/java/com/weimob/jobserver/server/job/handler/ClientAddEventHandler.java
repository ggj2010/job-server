package com.weimob.jobserver.server.job.handler;

import com.alibaba.fastjson.JSON;
import com.weimob.jobserver.core.job.dynamic.DynamicJobManager;
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

/**
 * @author: kevin
 * @date 2016/08/21.
 */
@Log4j
@Component
public class ClientAddEventHandler implements BaseEventHandler<JobConfiguration> {
    @Autowired
    private DynamicJobManager jobManager;
    @Autowired
    private JobConfigService jobConfigService;
    @Autowired
    private ServerConfigEntityService serverConfigService;

    @Override
    public JobEventType getEventType() {
        return JobEventType.CLIENT_ADD;
    }

    @Override
    public void onChange(JobConfiguration jobConfig) {
        try {
            JobConfigEntity jobConfigEntity = jobConfigService.get(jobConfig.getJobName(), jobConfig.getSystemId());
            if (jobConfigEntity == null) {
                log.warn("onChange error job does not exists data:" + JSON.toJSONString(jobConfig));
                return;
            }
            ServerConfigEntity serverConfigEntity = new ServerConfigEntity(jobConfigEntity.getId(), jobConfig.getIp(), 1);
            ServerConfigEntity persistEntity = serverConfigService.get(serverConfigEntity);
            if (persistEntity == null) {
                serverConfigService.save(serverConfigEntity);
            } else {
                serverConfigEntity.setId(persistEntity.getId());
                serverConfigService.update(serverConfigEntity);
            }
        } catch (Exception e) {
            log.error("onChange error param:" + JSON.toJSONString(jobConfig), e);
        }
    }
}
