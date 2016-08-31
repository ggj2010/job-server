package com.weimob.jobserver.server.job.database.base.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Map;

/**
 * @author: kevin
 * @date 2016/08/11.
 */
public abstract class BaseEntity implements Serializable {
    private Integer id;
    protected Map<String, String> sqlMap;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @JsonIgnore
    public Map<String, String> getSqlMap() {
        return sqlMap;
    }

    public void setSqlMap(Map<String, String> sqlMap) {
        this.sqlMap = sqlMap;
    }
}
