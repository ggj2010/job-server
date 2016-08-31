package com.weimob.jobserver.server.job.manager;

import com.alibaba.fastjson.JSON;
import com.weimob.jobserver.core.redis.RedisClientUtil;
import com.weimob.jobserver.core.reg.constant.JobLockType;
import com.weimob.jobserver.server.job.database.baen.JobConfigEntity;
import com.weimob.jobserver.server.job.database.baen.ServerConfigEntity;
import com.weimob.jobserver.server.job.database.service.JobConfigService;
import com.weimob.jobserver.server.job.database.service.ServerConfigEntityService;
import com.weimob.jobserver.server.job.executor.selection.Server;
import com.weimob.jobserver.server.job.executor.selection.WeightedRoundRobinScheduling;
import com.weimob.jobserver.server.utils.SpringContextHolder;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: kevin
 * @date 2016/08/11.
 */
@Log4j
@Component
public class JobManager {
    @Autowired
    private JobConfigService jobConfigService;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    @Autowired
    public RedisClientUtil redisSimpleClient;

    public JobConfigEntity getWorkingServers(Integer jobId) {
        JobConfigEntity jobConfigEntity = jobConfigService.get(jobId);
        JobLockType lockType = JobLockType.valueOf(jobConfigEntity.getLockStratergy());
        ServerConfigEntityService serverConfigService = SpringContextHolder.getBean(ServerConfigEntityService.class);
        ServerConfigEntity serverConfigEntity = new ServerConfigEntity(jobId);
        List<ServerConfigEntity> serverConfigEntities = null;
        try {
            serverConfigEntities = serverConfigService.findByJobId(jobId);
        } catch (Exception e) {
            log.error("findList error param:" + JSON.toJSONString(serverConfigEntity), e);
        }
        if (CollectionUtils.isEmpty(serverConfigEntities)) {
            return null;
        }
        if (serverConfigEntities.size() == 1) {
            jobConfigEntity.setServerConfigList(serverConfigEntities);
            return jobConfigEntity;
        }
        switch (lockType) {
            case OVVERAL_UNIQUE:
                WeightedRoundRobinScheduling weightedRoundRobinScheduling = new WeightedRoundRobinScheduling();
                List<Server> serverList = new ArrayList<>(serverConfigEntities.size());
                Map<Integer, ServerConfigEntity> serverMap = new HashMap<>();
                for (ServerConfigEntity configEntity : serverConfigEntities) {
                    Server server = new Server(configEntity.getServerIp(), configEntity.getWeight(), configEntity.getId());
                    serverList.add(server);
                    serverMap.put(configEntity.getId(), configEntity);
                }
                weightedRoundRobinScheduling.init(serverList, getLastRoundRobinIndex(jobId));
                Server selectedServer = weightedRoundRobinScheduling.getServer();
                serverConfigEntities = new ArrayList<>(1);
                setLastRoundRobinIndex(jobId, weightedRoundRobinScheduling.getCurrentIndex());
                serverConfigEntities.add(serverMap.get(selectedServer.getServerId()));
                break;
        }
        jobConfigEntity.setServerConfigList(serverConfigEntities);
        return jobConfigEntity;
    }

    /**
     * 获取上一次加权轮询的位置
     *
     * @param jobId
     * @return
     */
    private Integer getLastRoundRobinIndex(Integer jobId) {
        Integer lastPos = -1;
        String roundRobinIndex = redisSimpleClient.get("round_robin_" + jobId);
        if (!StringUtils.isEmpty(roundRobinIndex)) {
            lastPos = Integer.parseInt(roundRobinIndex);
        }
        return lastPos;
    }

    private void setLastRoundRobinIndex(Integer jobId, Integer index) {
        redisSimpleClient.set("round_robin_" + jobId, Integer.toString(index));
    }
}
