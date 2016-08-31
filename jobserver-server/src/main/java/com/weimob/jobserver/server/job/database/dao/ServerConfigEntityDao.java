package com.weimob.jobserver.server.job.database.dao;

import com.weimob.jobserver.server.job.database.baen.ServerConfigEntity;
import com.weimob.jobserver.server.job.database.base.annotation.MyBatisDao;
import com.weimob.jobserver.server.job.database.base.dao.CrudDao;

/**
 * @author: kevin
 * @date 2016/08/22.
 */
@MyBatisDao
public interface ServerConfigEntityDao extends CrudDao<ServerConfigEntity> {
    Integer countServers(Integer jobId);
}
