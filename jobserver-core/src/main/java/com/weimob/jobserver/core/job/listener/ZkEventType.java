package com.weimob.jobserver.core.job.listener;

/**
 * @author: kevin
 * @date 2016/08/03.
 */
public enum ZkEventType {
    NODE_ADDED,
    NODE_UPDATED,
    NODE_REMOVED,
    CONNECTION_SUSPENDED,
    CONNECTION_RECONNECTED,
    CONNECTION_LOST,
    INITIALIZED;
}
