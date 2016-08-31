package com.weimob.jobserver.core.job.listener;

/**
 * @author: kevin
 * @date 2016/08/21.
 */
public interface ZkDataChangeListener {
    void eventHandle(String dataJson, String path) throws Exception;
}
