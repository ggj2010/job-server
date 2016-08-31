package com.weimob.jobserver.server.job.database.base.service;

import com.weimob.jobserver.server.job.database.base.bean.BaseEntity;
import com.weimob.jobserver.server.job.database.base.dao.CrudDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;

/**
 * @author: kevin
 * @date 2016/08/11.
 */
@Transactional
public abstract class CrudService<D extends CrudDao<T>, T extends BaseEntity> {
    /**
     * 持久层对象
     */
    @Autowired
    protected D dao;

    /**
     * 获取单条数据
     *
     * @param id
     * @return
     */
    public T get(Integer id) {
        return dao.get(id);
    }

    /**
     * 获取单条数据
     *
     * @param entity
     * @return
     */
    public T get(T entity) {
        return dao.getByEntity(entity);
    }

    /**
     * 查询列表数据
     *
     * @param entity
     * @return
     */
    public List<T> findList(T entity) {
        return dao.findList(entity);
    }


    /**
     * 保存数据（插入或更新）
     *
     * @param entity
     */
    @Transactional(readOnly = false)
    public Integer save(T entity) throws SQLException {
        return dao.insert(entity);
    }

    @Transactional(readOnly = false)
    public int update(T t) {
        return dao.update(t);
    }
}
