package com.weimob.jobserver.server.event;

import com.alibaba.fastjson.JSON;
import com.weimob.jobserver.server.event.data.JobServerEvent;
import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: kevin
 * @date 2016/08/10.
 */
@Log4j
@Component
public class EventProducer<T> {
    @Autowired
    private EventSource eventSource;

    public void sendMsg(JobServerEvent<T> jobServerEvent) {
        try {
            EventData<T> eventData = new EventData(jobServerEvent);
            eventSource.sendMessage(eventData);
        } catch (Exception e) {
            log.error("EventProducer sendMessage error:" + JSON.toJSONString(jobServerEvent), e);
        }
    }
}

