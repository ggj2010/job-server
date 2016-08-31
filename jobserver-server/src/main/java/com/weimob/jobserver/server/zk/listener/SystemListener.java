package com.weimob.jobserver.server.zk.listener;

import com.alibaba.fastjson.JSON;
import com.weimob.jobserver.core.job.listener.ZkEventListener;
import com.weimob.jobserver.core.zk.ZookeeperRegistryCenter;
import com.weimob.jobserver.server.event.EventProducer;
import com.weimob.jobserver.server.event.data.JobEventType;
import com.weimob.jobserver.server.event.data.JobServerEvent;
import com.weimob.jobserver.server.utils.SpringContextHolder;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.recipes.cache.ChildData;

import java.util.HashSet;
import java.util.Set;

import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;

/**
 * @author: kevin
 * @date 2016/08/20.
 */
@Log4j
public class SystemListener implements ZkEventListener {
    private ZookeeperRegistryCenter zkCenter;

    private EventProducer eventProducer;
    private final JobListener jobListener;
    private Set<String> registeredSet = new HashSet<>();//缓存添加监控的系统，避免重复监听

    public SystemListener() {
        this.zkCenter = SpringContextHolder.getBean(ZookeeperRegistryCenter.class);
        this.eventProducer = SpringContextHolder.getBean(EventProducer.class);
        jobListener = new JobListener();
    }

    @Override
    public void eventHandle(Type type, ChildData childData, String path) {
        try {
            if (childData == null) {
                return;
            }
            String serverNodePath = childData.getPath();
            if (StringUtils.isEmpty(serverNodePath)) {
                return;
            }
            switch (type) {
                case CHILD_ADDED:
                    if (!registeredSet.contains(serverNodePath)) {
                        zkCenter.addChildWatcher(serverNodePath, jobListener);
                        registeredSet.add(serverNodePath);
                    }
                    break;
                case CHILD_UPDATED:
                    break;
                case CHILD_REMOVED:
                    serverNodePath = serverNodePath.replace("/", "");
                    eventProducer.sendMsg(new JobServerEvent(serverNodePath, JobEventType.DELETE_SYSTEM));
                    break;
            }
        } catch (Exception e) {
            log.error("eventHandle error", e);
        }
    }
}
