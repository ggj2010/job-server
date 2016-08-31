package com.weimob.jobserver.server.job.executor;

import com.alibaba.fastjson.JSON;
import com.weimob.jobserver.core.job.commond.CommondType;
import com.weimob.jobserver.core.job.commond.JobCommond;
import com.weimob.jobserver.server.http.HttpClientUtils;
import com.weimob.jobserver.server.job.database.baen.JobConfigEntity;
import com.weimob.jobserver.server.job.database.baen.ServerConfigEntity;
import com.weimob.jobserver.server.job.manager.JobManager;
import com.weimob.jobserver.server.utils.SpringContextHolder;
import lombok.extern.log4j.Log4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;

/**
 * @author: kevin
 * @date 2016/08/21.
 */
@Log4j
public class HttpJobExecutor extends BaseRemoteJobExecutor {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Integer jobId = (Integer) jobExecutionContext.getMergedJobDataMap().get("jobId");
        JobManager jobManager = SpringContextHolder.getBean(JobManager.class);
        final JobConfigEntity jobConfigEntity = jobManager.getWorkingServers(jobId);
        List<ServerConfigEntity> serverConfigEntities = jobConfigEntity.getServerConfigList();
        if (CollectionUtils.isEmpty(serverConfigEntities)) {
            log.warn(String.format("job [%s] jobName[%s] has no available server", jobId, jobConfigEntity.getJobName()));
            return;
        }
        ThreadPoolTaskExecutor taskExecutor = SpringContextHolder.getBean(ThreadPoolTaskExecutor.class);
        for (final ServerConfigEntity serverConfigEntity : serverConfigEntities) {
            taskExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    StringBuilder urlBuilder = new StringBuilder("http://").append(serverConfigEntity.getServerIp())
                            .append(":").append(serverConfigEntity.getServerPort());
                    if (!StringUtils.isEmpty(serverConfigEntity.getClientName())) {
                        urlBuilder.append("/").append(serverConfigEntity.getClientName());
                    }
                    urlBuilder.append("/jobserver");
                    JobCommond jobCommond = new JobCommond(CommondType.TRIGGER);
                    jobCommond.setJobName(jobConfigEntity.getJobName());
                    String jobCommondJson = JSON.toJSONString(jobCommond);
                    HttpClientUtils.doPost(urlBuilder.toString(), jobCommondJson);
                }
            });
        }
    }
}
