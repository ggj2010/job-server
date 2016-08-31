package com.weimob.jobserver.server.job.database.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.weimob.jobserver.core.redis.RedisClientUtil;
import com.weimob.jobserver.core.reg.constant.JobLockType;
import com.weimob.jobserver.core.reg.constant.JobStatus;
import com.weimob.jobserver.core.reg.constant.JobType;
import com.weimob.jobserver.server.job.database.baen.JobConfigEntity;
import com.weimob.jobserver.server.job.database.base.service.CrudService;
import com.weimob.jobserver.server.job.database.constant.RedisKeyConfig;
import com.weimob.jobserver.server.job.database.dao.JobConfigDao;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;


/**
 * @author: kevin
 * @date 2016/08/11.
 */
@Log4j
@Service
public class JobConfigService extends CrudService<JobConfigDao, JobConfigEntity> {

    @Autowired
    public RedisClientUtil redisSimpleClient;

    public JobConfigEntity get(String jobName, String systemId) {
        return super.get(new JobConfigEntity(jobName, systemId));
    }

    public JobConfigEntity refreshCache(Integer id) {
        JobConfigEntity jobConfigEntity = super.get(id);
        setJobConfigRedis(jobConfigEntity);
        return jobConfigEntity;
    }

    @Override
    public JobConfigEntity get(Integer id) {
        JobConfigEntity cache = getRedisJobConfig(id);
        if (cache != null) {
            return cache;
        }
        cache = super.get(id);
        if (cache != null) {
            setJobConfigRedis(cache);
        }
        return cache;
    }

    public List<JobConfigEntity> loadInitJobs(JobConfigEntity jobConfigEntity) {
        return dao.loadInitJobs(jobConfigEntity);
    }

    /**
     * 查询未删除任务
     *
     * @param jobConfigEntity
     * @return
     */
    public List<JobConfigEntity> findAvaliableJobs(JobConfigEntity jobConfigEntity) {
        return dao.findAvaliableJobs(jobConfigEntity);
    }

    @Override
    public int update(JobConfigEntity jobConfigEntity) {
        Integer result = super.update(jobConfigEntity);
        if (result > 0) {
            setJobConfigRedis(jobConfigEntity);
        }
        return result;
    }

    public List<JobConfigEntity> findByIds(Set<Integer> ids) {
        return dao.findByIds(ids);
    }

    public List<JobConfigEntity> findByIdAndParam(Set<Integer> ids, JobConfigEntity jobConfigEntity) {
        return dao.findByIdAndParam(ids, jobConfigEntity);
    }

    /**
     * 添加任务
     *
     * @param jobConfigEntity
     * @return
     * @throws SQLException
     */
    @Transactional(readOnly = false)
    public JobConfigEntity insert(JobConfigEntity jobConfigEntity) throws SQLException {
        super.save(jobConfigEntity);
        setJobConfigRedis(jobConfigEntity);
        return jobConfigEntity;
    }

    public boolean delete(JobConfigEntity jobConfigEntity) {
        delJobConfigRedis(jobConfigEntity);
        dao.delete(jobConfigEntity);
        return true;
    }


    private void setJobConfigRedis(JobConfigEntity jobConfigEntity) {
        try {
            if (jobConfigEntity.getId() != null && jobConfigEntity.getId() > 0) {
                String jobRedisKey = RedisKeyConfig.JOB_KEY.replace("{ID}", Integer.toString(jobConfigEntity.getId()));
                redisSimpleClient.set(jobRedisKey, JSON.toJSONString(jobConfigEntity));
            }
        } catch (Exception e) {
            log.error("setJobConfigRedis error,jobConfig:" + JSON.toJSONString(jobConfigEntity), e);
        }
    }

    private void delJobConfigRedis(JobConfigEntity jobConfigEntity) {
        try {
            String jobRedisKey = RedisKeyConfig.JOB_KEY.replace("{ID}", Integer.toString(jobConfigEntity.getId()));
            redisSimpleClient.del(jobRedisKey);
        } catch (Exception e) {
            log.error("delJobConfigRedis error,jobConfig:" + JSON.toJSONString(jobConfigEntity), e);
        }
    }

    private JobConfigEntity getRedisJobConfig(Integer jobId) {
        String jobRedisKey = RedisKeyConfig.JOB_KEY.replace("{ID}", Integer.toString(jobId));
        try {
            String result = redisSimpleClient.get(jobRedisKey);
            if (StringUtils.isEmpty(result)) {
                return null;
            }
            return JSONObject.parseObject(result, JobConfigEntity.class);
        } catch (Exception e) {
            log.error("getRedisJobConfig error jobId:" + jobId, e);
        }
        return null;
    }

    public List<String> getSystemList(JobConfigEntity jobConfigEntity) {
        return dao.getSystemList(jobConfigEntity);
    }

    public void updateSystemId(String oldSystemId, String newSystemId) {
        dao.updateSystemId(oldSystemId, newSystemId);
    }


    /**
     * 获取job状态信息
     *
     * @param jobStatus
     * @return
     */
    public Pair<String, Integer> getJobStatus(JobStatus jobStatus) {
        MutablePair<String, Integer> mutablePair = new MutablePair<>();
        switch (jobStatus) {
            case DELETED:
                mutablePair.setLeft("已经删除");
                mutablePair.setRight(4);
                break;
            case EXECUTING:
                mutablePair.setLeft("正在运行");
                mutablePair.setRight(3);
                break;
            case PAUSED:
                mutablePair.setLeft("暂停执行");
                mutablePair.setRight(2);
                break;
            case ERROR:
                mutablePair.setLeft("执行出错");
                mutablePair.setRight(3);
                break;
        }
        return mutablePair;
    }

    public String getJobType(JobType jobType) {
        switch (jobType) {
            case LOCAL:
                return "本地任务";
            case ZK_JOB:
            case HTTP_JOB:
                return "远程任务";
        }
        return "";
    }

    public String getLockStratery(JobLockType lockStrategy) {
        switch (lockStrategy) {
            case OVVERAL_UNIQUE:
                return "全局唯一";
            case DEFAULT:
                return "不加锁";
        }
        return "";
    }
}
