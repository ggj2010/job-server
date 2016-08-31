package com.weimob.jobserver.server.job.database.base.dao;

import java.util.List;

/**
 * @author: kevin
 * @date 2016/08/11.
 */
public interface CrudDao<T> {

    T get(Integer id);

    T getByEntity(T entity);

    List<T> findList(T entity);

    List<T> findAllList(T entity);

    int insert(T entity);

    int update(T entity);

    int delete(T entity);
}
