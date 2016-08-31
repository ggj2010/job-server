package com.weimob.jobserver.server.job.database.service;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.weimob.jobserver.server.job.database.baen.JobLogBean;
import com.weimob.jobserver.server.job.database.base.service.CrudService;
import com.weimob.jobserver.server.job.database.dao.JobLogInfoDao;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author: kevin
 * @date 2016/08/11.
 */
@Service
public class JobLogInfoService extends CrudService<JobLogInfoDao, JobLogBean> {
    public void cleanLog() {
        dao.cleanLog();
    }

    @Override
    public Integer save(JobLogBean entity) throws SQLException {
        entity.setUpdateTime(new Date());
        return super.save(entity);
    }

    public Page<JobLogBean> findList(JobLogBean entity) {
        Integer startPage = (entity.getPageNum() == null || entity.getPageNum() <= 0) ? 1 : entity.getPageNum();
        Integer pageSize = entity.getPageSize();
        Page<JobLogBean> pageResult = PageHelper.startPage(startPage, pageSize);
        List<JobLogBean> logBeenList = dao.findList(entity);
        pageResult.addAll(logBeenList);
        return pageResult;
    }

    public Page<JobLogBean> findByJobIds(Set<Integer> jobIds, JobLogBean logBean) {
        if (CollectionUtils.isEmpty(jobIds)) {
            return new Page<>();
        }
        Integer startPage = (logBean.getPageNum() == null || logBean.getPageNum() <= 0) ? 1 : logBean.getPageNum();
        Integer pageSize = logBean.getPageSize();
        Page<JobLogBean> pageResult = PageHelper.startPage(startPage, pageSize);
        pageResult.addAll(dao.findByJobIds(jobIds, logBean));
        return pageResult;
    }

}
