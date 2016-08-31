package com.weimob.jobserver.server.job.handler;

import com.weimob.jobserver.core.reg.domain.JobConfiguration;
import com.weimob.jobserver.server.event.data.JobEventType;
import com.weimob.jobserver.server.event.handler.BaseEventHandler;

/**
 * @author: kevin
 * @date 2016/08/25.
 */
public class ServerErrorHandler implements BaseEventHandler<JobConfiguration> {
    @Override
    public JobEventType getEventType() {
        return null;
    }

    @Override
    public void onChange(JobConfiguration jobConfig) {

    }
}
