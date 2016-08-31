package com.weimob.jobserver.server.job.database.dao;

import com.weimob.jobserver.server.job.database.baen.JobLogBean;
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
public interface JobLogInfoDao extends CrudDao<JobLogBean> {
    void cleanLog();

    List<JobLogBean> findByJobIds(@Param("jobIdSet") Set<Integer> jobIds, @Param("logBean") JobLogBean logBean);
}
