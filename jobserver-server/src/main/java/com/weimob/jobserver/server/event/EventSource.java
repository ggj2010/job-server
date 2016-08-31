package com.weimob.jobserver.server.event;

import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

@Log4j
@Component
class EventSource implements ApplicationEventPublisherAware {
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void sendMessage(EventData eventData) {
        try {
            applicationEventPublisher.publishEvent(eventData);
        } catch (Exception e) {
            log.error("sendMessage error " + JSON.toJSONString(eventData), e);
        }
    }
}