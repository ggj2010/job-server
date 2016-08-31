package com.weimob.jobserver.server.job.manager;


import com.weimob.jobserver.server.job.database.service.JobConfigService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author: kevin
 * @date 2016/08/12.
 */
@Log4j
@Component
public class MonitorManager {
    @Autowired
    JobConfigService jobConfigService;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;


}
