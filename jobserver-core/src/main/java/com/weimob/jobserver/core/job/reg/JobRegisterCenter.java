package com.weimob.jobserver.core.job.reg;

import com.weimob.jobserver.core.job.BaseAbstractJob;

/**
 * @author: kevin
 * @date 2016/08/19.
 */
public interface JobRegisterCenter<T extends BaseAbstractJob> {
    void register(T job) throws Exception;
}
