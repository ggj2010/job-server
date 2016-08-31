package com.weimob.jobserver.server.job.database.dao;

import com.weimob.jobserver.server.job.database.baen.JobConfigEntity;
import com.weimob.jobserver.server.job.database.base.annotation.MyBatisDao;
import com.weimob.jobserver.server.job.database.base.dao.CrudDao;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author: kevin
 * @date 2016/08/11.
 */
@MyBatisDao
public interface JobConfigDao extends CrudDao<JobConfigEntity> {

    List<JobConfigEntity> loadInitJobs(JobConfigEntity jobConfigEntity);

    List<JobConfigEntity> findByIds(@Param("ids") Set<Integer> ids);

    List<JobConfigEntity> findByIdAndParam(@Param("ids") Set<Integer> ids, @Param("jobConfig") JobConfigEntity jobConfigEntity);

    /**
     * 获取systemId列表
     *
     * @param jobConfigEntity
     * @return
     */
    List<String> getSystemList(JobConfigEntity jobConfigEntity);

    void updateSystemId(@Param("oldSystemId") String oldSystemId, @Param("newSystemId") String newSystemId);

    List<JobConfigEntity> findAvaliableJobs(JobConfigEntity jobConfigEntity);
}
