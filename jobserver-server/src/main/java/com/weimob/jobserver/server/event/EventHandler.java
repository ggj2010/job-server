package com.weimob.jobserver.server.event;

import com.alibaba.fastjson.JSON;
import com.weimob.jobserver.server.event.data.JobEventType;
import com.weimob.jobserver.server.event.data.JobServerEvent;
import com.weimob.jobserver.server.event.handler.BaseEventHandler;

import lombok.extern.log4j.Log4j;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 数据变化处理
 */
@Log4j
@Component
public class EventHandler<T> implements ApplicationListener<EventData<T>> {
    private Map<JobEventType, BaseEventHandler> eventHandlerMap;

    @Override
    public void onApplicationEvent(EventData eventData) {
        if (eventHandlerMap == null) {
            log.error("eventHandlerMap is null");
            return;
        }
        Object source = eventData.getSource();
        if (source instanceof JobServerEvent) {
            JobServerEvent<T> jobServerEvent = (JobServerEvent) source;
            JobEventType jobEventType = jobServerEvent.getServerJobEventType();
            BaseEventHandler<T> eventHandler = eventHandlerMap.get(jobEventType);
            if (eventHandler != null) {
                T serverJobEntity = jobServerEvent.getEventData();
                if (serverJobEntity != null) {
                    eventHandler.onChange(serverJobEntity);
                } else {
                    log.warn(String.format("event data is null: " + JSON.toJSONString(source)));
                }
            }
        }
    }

    public void setEventHandlerMap(Map<JobEventType, BaseEventHandler> eventHandlerMap) {
        this.eventHandlerMap = eventHandlerMap;
    }
}