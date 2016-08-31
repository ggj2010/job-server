package com.weimob.jobserver.server.zk.listener;

import com.alibaba.fastjson.JSONObject;
import com.weimob.jobserver.core.job.listener.ZkEventListener;
import com.weimob.jobserver.core.reg.domain.RemoteJobConfig;
import com.weimob.jobserver.core.reg.storage.JobNodePath;
import com.weimob.jobserver.core.zk.ZookeeperRegistryCenter;
import com.weimob.jobserver.core.zk.utils.ZkPathUtil;
import com.weimob.jobserver.server.event.EventProducer;
import com.weimob.jobserver.server.event.data.JobEventType;
import com.weimob.jobserver.server.event.data.JobServerEvent;
import com.weimob.jobserver.server.utils.SpringContextHolder;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.recipes.cache.ChildData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;

/**
 * @author: kevin
 * @date 2016/08/20.
 */
@Log4j
public class JobListener implements ZkEventListener {
    private ZookeeperRegistryCenter zkCenter;
    private EventProducer eventProducer;
    private final JobConfigListener jobConfigListener;
    private final JobDataListener jobDataListener;
    private final JobRemoteServerListener remoteServerListener;
    private Set<String> registeredSet = new HashSet<>();

    public JobListener() {
        this.zkCenter = SpringContextHolder.getBean(ZookeeperRegistryCenter.class);
        this.eventProducer = SpringContextHolder.getBean(EventProducer.class);
        jobConfigListener = new JobConfigListener();
        jobDataListener = new JobDataListener();
        remoteServerListener = new JobRemoteServerListener();
    }

    @Override
    public void eventHandle(Type type, ChildData childData, String path) {
        try {
            if (childData == null) {
                return;
            }
            List<String> nodePathList = ZkPathUtil.parseZkPath(childData.getPath());
            String systemId = nodePathList.get(0);
            String jobName = nodePathList.get(1);
            JobNodePath jobNodePath = new JobNodePath(systemId, jobName);
            String jobConfigJson = zkCenter.get(jobNodePath.getJobDataPath());
            if (StringUtils.isEmpty(jobConfigJson)) {
                log.warn("no config data found for path:" + jobNodePath.getJobDataPath());
                zkCenter.remove(jobNodePath.getJobPath());
                return;
            }
            RemoteJobConfig jobConfig = JSONObject.parseObject(jobConfigJson, RemoteJobConfig.class);
            String serverNodePath = jobNodePath.getServerNodePath();
            String configNodePath = jobNodePath.getConfigNodePath();
            String jobDataPath = jobNodePath.getJobDataPath();
            log.info(String.format("jobListener type:{%s}, nodePathList{%s}", type, nodePathList));
            switch (type) {
                case CHILD_ADDED:
                    if (!registeredSet.contains(serverNodePath)) {
                        zkCenter.addChildWatcher(serverNodePath, remoteServerListener);
                        registeredSet.add(serverNodePath);
                    }
                    if (!registeredSet.contains(configNodePath)) {
                        zkCenter.addNodeCacheWatcher(configNodePath, jobConfigListener);
                        registeredSet.add(configNodePath);
                    }
                    if (!registeredSet.contains(jobDataPath)) {
                        zkCenter.addNodeCacheWatcher(jobDataPath, jobDataListener);
                        registeredSet.add(jobDataPath);
                    }
                    eventProducer.sendMsg(new JobServerEvent(jobConfig, JobEventType.ADD_UPDATE_JOB));
                    break;
                case CHILD_UPDATED:
                    eventProducer.sendMsg(new JobServerEvent(jobConfig, JobEventType.ADD_UPDATE_JOB));
                    break;
            }
        } catch (Exception e) {
            log.error("eventHandle error", e);
        }
    }
}
