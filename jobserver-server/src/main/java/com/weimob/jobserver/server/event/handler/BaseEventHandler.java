package com.weimob.jobserver.server.event.handler;

import com.weimob.jobserver.server.event.data.JobEventType;

/**
 * @author: kevin
 * @date 2016/08/10.
 */
public interface BaseEventHandler<T> {
    JobEventType getEventType();

    void onChange(T jobConfig);
}
