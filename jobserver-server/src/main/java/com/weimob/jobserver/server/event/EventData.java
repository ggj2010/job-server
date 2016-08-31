package com.weimob.jobserver.server.event;

import com.weimob.jobserver.server.event.data.JobServerEvent;
import org.springframework.context.ApplicationEvent;

/**
 * @author: kevin
 * @date 2016/08/10.
 */
class EventData<T> extends ApplicationEvent {
    public EventData(JobServerEvent<T> source) {
        super(source);
    }
}
