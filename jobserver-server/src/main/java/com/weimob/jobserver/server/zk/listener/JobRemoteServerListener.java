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
import org.apache.commons.collections.CollectionUtils;
import org.apache.curator.framework.recipes.cache.ChildData;

import java.util.List;

import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;

/**
 * @author: kevin
 * @date 2016/08/20.
 */
@Log4j
public class JobRemoteServerListener implements ZkEventListener {
    private ZookeeperRegistryCenter zkCenter;
    private EventProducer eventProducer;

    public JobRemoteServerListener() {
        this.zkCenter = SpringContextHolder.getBean(ZookeeperRegistryCenter.class);
        this.eventProducer = SpringContextHolder.getBean(EventProducer.class);
    }

    @Override
    public void eventHandle(Type type, ChildData childData, String path) throws Exception {
        if (childData == null) {
            return;
        }
        String serverIpPath = null;
        try {
            serverIpPath = childData.getPath();
            List<String> pathList = ZkPathUtil.parseZkPath(serverIpPath);
            if (CollectionUtils.isEmpty(pathList) || pathList.size() < 3) {
                return;
            }
            String serverIp = serverIpPath.substring(serverIpPath.lastIndexOf("/")).replace("/", "");
            String systemId = pathList.get(0);
            String jobName = pathList.get(1);
            JobNodePath jobNodePath = new JobNodePath(systemId, jobName);
            String jobConfigJson = zkCenter.get(jobNodePath.getJobDataPath());
            RemoteJobConfig jobConfig = JSONObject.parseObject(jobConfigJson, RemoteJobConfig.class);
            jobConfig.setIp(serverIp);
            switch (type) {
                case CHILD_ADDED:
                    eventProducer.sendMsg(new JobServerEvent(jobConfig, JobEventType.CLIENT_ADD));
                    break;
                case CHILD_REMOVED:
                    eventProducer.sendMsg(new JobServerEvent(jobConfig, JobEventType.CLIENT_REMOVE));
                    break;
            }
        } catch (Exception e) {
            log.error("eventHandle error,serverIpPath:" + serverIpPath, e);
        }
    }
}
