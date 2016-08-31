package com.weimob.jobserver.server.job.log;

import com.alibaba.fastjson.JSONObject;
import com.weimob.jobserver.core.job.log.JobLogInfo;
import com.weimob.jobserver.core.redis.RedisClientUtil;
import com.weimob.jobserver.core.tools.CommonConstants;
import com.weimob.jobserver.server.job.database.baen.JobConfigEntity;
import com.weimob.jobserver.server.job.database.baen.JobLogBean;
import com.weimob.jobserver.server.job.database.baen.ServerConfigEntity;
import com.weimob.jobserver.server.job.database.service.JobConfigService;
import com.weimob.jobserver.server.job.database.service.JobLogInfoService;
import com.weimob.jobserver.server.job.database.service.ServerConfigEntityService;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.ShardedJedis;

import java.sql.SQLException;
import java.util.Date;

/**
 * @author: kevin
 * @date 2016/08/16.
 */
@Log4j
@Component
public class LogConsumerJob {
    @Autowired
    JobLogInfoService logInfoService;
    @Autowired
    private JobConfigService jobConfigService;
    @Autowired
    private ServerConfigEntityService serverConfigService;
    @Autowired
    public RedisClientUtil redisSimpleClient;
    private ShardedJedis jedis;

    /**
     * 处理log
     */
    @Scheduled(fixedDelay = 5000)
    public void processLog() {
        String logInfo = null;
        jedis = redisSimpleClient.getResource();
        try {
            logInfo = jedis.lpop(CommonConstants.LOG_REDIS_KEY);
            if (!StringUtils.isEmpty(logInfo)) {
                saveLog(logInfo);
            }
        } catch (Exception e) {
            log.error("processLog error,logMessage is:" + logInfo, e);
        } finally {
            redisSimpleClient.returnResourceObject(jedis);
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨1点执行
    public void logClean() {
        logInfoService.cleanLog();
    }

    private void saveLog(String logStr) throws SQLException {
        JobLogInfo logData = JSONObject.parseObject(logStr, JobLogInfo.class);
        if (logData == null) {
            return;
        }
        JobConfigEntity jobConfigEntity = jobConfigService.get(logData.getJobName(), logData.getSystemId());
        if (jobConfigEntity == null) {
            return;
        }
        ServerConfigEntity serverConfigEntity = new ServerConfigEntity();
        serverConfigEntity.setServerIp(logData.getClientIp());
        serverConfigEntity.setServerPort(logData.getClientPort());
        serverConfigEntity.setJobId(jobConfigEntity.getId());
        serverConfigEntity = serverConfigService.get(serverConfigEntity);
        if (serverConfigEntity == null) {
            return;
        }
        JobLogBean logInfo = new JobLogBean(jobConfigEntity.getId(), serverConfigEntity.getId());
        logInfo.setSuccess(logData.getSuccess());
        logInfo.setEndTime(logData.getEndTime());
        logInfo.setUpdateTime(new Date());
        logInfo.setStartTime(logData.getStartTime());
        logInfoService.save(logInfo);
        String logInfoStr = jedis.lpop(CommonConstants.LOG_REDIS_KEY);
        if (!StringUtils.isEmpty(logInfoStr)) {
            saveLog(logInfoStr);
        }
    }
}
