package com.weimob.jobserver.server.job.database.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.weimob.jobserver.core.redis.RedisClientUtil;
import com.weimob.jobserver.server.job.database.baen.ServerConfigEntity;
import com.weimob.jobserver.server.job.database.base.service.CrudService;
import com.weimob.jobserver.server.job.database.constant.RedisKeyConfig;
import com.weimob.jobserver.server.job.database.dao.ServerConfigEntityDao;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

/**
 * @author: kevin
 * @date 2016/08/22.
 */
@Log4j
@Service
public class ServerConfigEntityService extends CrudService<ServerConfigEntityDao, ServerConfigEntity> {
    @Autowired
    public RedisClientUtil redisSimpleClient;

    public void delete(ServerConfigEntity serverConfigEntity) {
        dao.delete(serverConfigEntity);
    }

    public Integer countServers(Integer jobId) {
        return dao.countServers(jobId);
    }

    @Override
    public List<ServerConfigEntity> findList(ServerConfigEntity entity) {
        return super.findList(entity);
    }

    @Override
    public Integer save(ServerConfigEntity entity) throws SQLException {
        Integer result = super.save(entity);
        refreshCache(entity.getJobId());
        return result;
    }

    @Override
    public int update(ServerConfigEntity serverConfigEntity) {
        int result = super.update(serverConfigEntity);
        refreshCache(serverConfigEntity.getJobId());
        return result;
    }

    public List<ServerConfigEntity> findByJobId(Integer jobId) {
        List<ServerConfigEntity> serverConfigEntities = findCacheByJobId(jobId);
        if (!CollectionUtils.isEmpty(serverConfigEntities)) {
            return serverConfigEntities;
        }
        serverConfigEntities = super.findList(new ServerConfigEntity(jobId));
        cacheServerConfigList(jobId, serverConfigEntities);
        return serverConfigEntities;
    }

    private List<ServerConfigEntity> findCacheByJobId(Integer jobId) {
        String jobRedisKey = RedisKeyConfig.JOB_SERVERS.replace("{ID}", Integer.toString(jobId));
        try {
            String result = redisSimpleClient.get(jobRedisKey);
            if (!StringUtils.isEmpty(result)) {
                return JSONObject.parseArray(result, ServerConfigEntity.class);
            }
        } catch (Exception e) {
            log.error("findCacheByJobId error jobId:" + jobId, e);
        }
        return null;
    }

    private void cacheServerConfigList(Integer jobId, List<ServerConfigEntity> serverConfigEntities) {
        if (CollectionUtils.isEmpty(serverConfigEntities)) {
            return;
        }
        String jobRedisKey = RedisKeyConfig.JOB_SERVERS.replace("{ID}", Integer.toString(jobId));
        try {
            redisSimpleClient.set(jobRedisKey, JSON.toJSONString(serverConfigEntities));
        } catch (Exception e) {
            log.error("cacheServerConfigList error", e);
        }
    }

    private void refreshCache(Integer jobId) {
        List<ServerConfigEntity> serverConfigEntities = super.findList(new ServerConfigEntity(jobId));
        cacheServerConfigList(jobId, serverConfigEntities);
    }
}
