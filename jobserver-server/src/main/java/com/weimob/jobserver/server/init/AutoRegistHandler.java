package com.weimob.jobserver.server.init;

import com.weimob.jobserver.server.event.EventHandler;
import com.weimob.jobserver.server.event.data.JobEventType;
import com.weimob.jobserver.server.event.handler.BaseEventHandler;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author: kevin
 * @date 2016/08/10.
 */
@Component
@Configuration
public class AutoRegistHandler implements InitializingBean, ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, BaseEventHandler> beanMap = applicationContext.getBeansOfType(BaseEventHandler.class);//注册事件的处理类
        Map<JobEventType, BaseEventHandler> eventHandlerMap = new HashMap<>();
        if (!MapUtils.isEmpty(beanMap)) {
            Iterator<String> iteratorKey = beanMap.keySet().iterator();
            while (iteratorKey.hasNext()) {
                BaseEventHandler baseEventHandler = beanMap.get(iteratorKey.next());
                if (baseEventHandler == null) {
                    break;
                }
                if (baseEventHandler.getClass().isInterface()) {//去掉interface
                    break;
                }
                JobEventType eventType = baseEventHandler.getEventType();
                if (eventType == null) {
                    break;
                }
                eventHandlerMap.put(eventType, baseEventHandler);
                EventHandler eventHandler = applicationContext.getBean(EventHandler.class);
                eventHandler.setEventHandlerMap(eventHandlerMap);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
