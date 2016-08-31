package com.weimob.jobserver.server.event.data;

import java.io.Serializable;

/**
 * @author: kevin
 * @date 2016/08/06.
 */
public class JobServerEvent<T> implements Serializable {

    private T eventData;
    private JobEventType serverJobEventType;

    public JobServerEvent(T eventData, JobEventType serverJobEventType) {
        this.eventData = eventData;
        this.serverJobEventType = serverJobEventType;
    }

    public T getEventData() {
        return eventData;
    }

    public void setEventData(T eventData) {
        this.eventData = eventData;
    }

    public JobEventType getServerJobEventType() {
        return serverJobEventType;
    }

    public void setServerJobEventType(JobEventType serverJobEventType) {
        this.serverJobEventType = serverJobEventType;
    }
}
