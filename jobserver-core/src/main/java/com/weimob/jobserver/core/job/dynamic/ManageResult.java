package com.weimob.jobserver.core.job.dynamic;

public enum ManageResult {
    RUNNING,
    INTERRUPTED,
    PAUSED,
    CLUSTER_RUNNING,
    NON_EXISTED,
    FAILED,
    CRON_CHANGED,
    CREATED,
    RESUMED
}
