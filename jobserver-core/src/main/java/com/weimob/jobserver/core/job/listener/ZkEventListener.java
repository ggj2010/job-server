package com.weimob.jobserver.core.job.listener;

import org.apache.curator.framework.recipes.cache.ChildData;

import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;

public interface ZkEventListener {
    void eventHandle(Type type, ChildData data, String path) throws Exception;
}