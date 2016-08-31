package com.weimob.jobserver.client.register;


import com.weimob.jobserver.client.http.JobServerInitServelet;
import com.weimob.jobserver.core.job.BaseAbstractJob;
import com.weimob.jobserver.core.job.LocalJob;
import com.weimob.jobserver.core.job.RemoteJob;
import com.weimob.jobserver.core.redis.RedisClientUtil;
import com.weimob.jobserver.core.reg.annotation.ScheduledJob;
import com.weimob.jobserver.core.reg.constant.JobStatus;
import com.weimob.jobserver.core.reg.constant.JobType;
import com.weimob.jobserver.core.reg.domain.JobConfiguration;
import com.weimob.jobserver.core.reg.domain.LocalJobConfig;
import com.weimob.jobserver.core.reg.domain.RemoteJobConfig;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: kevin
 * @date 2016/07/27.
 */
@Log4j
public class AutoRegister implements BeanFactoryAware,
        ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware, InitializingBean {
    private DefaultListableBeanFactory beanFactory;
    private static ApplicationContext applicationContext;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
        /**
         * register quatz
         */
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(SchedulerFactoryBean.class);
        beanDefinitionBuilder.addPropertyValue("startupDelay", 10);
        ((DefaultListableBeanFactory) beanFactory).registerBeanDefinition("schedulerFactoryBean", beanDefinitionBuilder.getRawBeanDefinition());

        /**
         * init config
         */

        try {
            initConstants();
        } catch (UnknownHostException e) {
            log.error("get local ip error", e);
        }
        /**
         * register redis
         */
        RedisClientUtil redisClientUtil = new RedisClientUtil();
        redisClientUtil.setIp(redisHost);
        if (!StringUtils.isEmpty(redisPort) && redisPort.matches("^[-\\\\+]?[\\\\d]*$")) {
            redisClientUtil.setPort(Integer.parseInt(redisPort));
        }
        redisClientUtil.setPassword(redisPassword);
        redisClientUtil.setMaxIdle(maxIdle);
        redisClientUtil.setMaxTotal(maxTotal);
        redisClientUtil.setMaxWaitMillis(maxWaitMills);

        BeanDefinitionBuilder redisClientFactoryBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(RedisClientUtil.class);
        redisClientFactoryBeanBuilder.addPropertyValue("ip", redisHost);
        redisClientFactoryBeanBuilder.addPropertyValue("port", redisPort);
        redisClientFactoryBeanBuilder.addPropertyValue("password", redisPassword);
        redisClientFactoryBeanBuilder.addPropertyValue("maxIdle", maxIdle);
        redisClientFactoryBeanBuilder.addPropertyValue("maxTotal", maxTotal);
        redisClientFactoryBeanBuilder.addPropertyValue("maxWaitMillis", maxWaitMills);
        redisClientFactoryBeanBuilder.setInitMethodName("initRedis");
        ((DefaultListableBeanFactory) beanFactory).registerBeanDefinition("redisClientUtil", redisClientFactoryBeanBuilder.getRawBeanDefinition());

    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, Object> remoteJobMap = applicationContext.getBeansWithAnnotation(ScheduledJob.class);
        if (remoteJobMap == null || remoteJobMap.size() <= 0) {
            return;
        }
        List<BaseAbstractJob> abstractJobs = new ArrayList<>(remoteJobMap.size());
        for (final Object myJob : remoteJobMap.values()) {
            ScheduledJob jobConfig = myJob.getClass().getAnnotation(ScheduledJob.class);
            if (jobConfig == null) {
                return;
            }
            JobConfiguration jobConfiguration;
            String jobClass = myJob.getClass().getName();
            if (myJob instanceof LocalJob) {
                LocalJob localJob = (LocalJob) myJob;
                jobConfiguration = new LocalJobConfig(jobConfig.jobName(), jobConfig.group(), jobConfig.cronExp(), jobClass, JobType.LOCAL, JobStatus.EXECUTING, InitConstants.systemId, InitConstants.clientIp, jobConfig.LOCK_TYPE());
                localJob.setConfig((LocalJobConfig) jobConfiguration);
                abstractJobs.add(localJob);
            } else if (myJob instanceof RemoteJob) {
                RemoteJob remoteJob = (RemoteJob) myJob;
                jobConfiguration = new RemoteJobConfig(jobConfig.jobName(), jobConfig.group(), jobConfig.cronExp(), jobClass, JobType.ZK_JOB, JobStatus.EXECUTING, InitConstants.systemId, InitConstants.clientIp, jobConfig.LOCK_TYPE(), InitConstants.clientName);
                abstractJobs.add(remoteJob);
                remoteJob.setConfig((RemoteJobConfig) jobConfiguration);
            }
        }
        SchedulerFactoryBean scheduler = applicationContext.getBean(SchedulerFactoryBean.class);
        RedisClientUtil redisClientUtil = (RedisClientUtil) applicationContext.getBean("redisClientUtil");
        JobManagerCenter jobManagerCenter = new JobManagerCenter(redisClientUtil, scheduler, abstractJobs);
        jobManagerCenter.init();
        JobServerInitServelet.jobManagerCenter = jobManagerCenter;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    /**
     * 获取方法参数类型
     *
     * @param method
     * @return
     */
    private String[] getParameterTypes(Method method) {
        Class<?>[] parameterTyps = method.getParameterTypes();
        if (parameterTyps != null && parameterTyps.length > 0) {
            String[] parameterTypeStr = new String[parameterTyps.length];
            for (int i = 0; i < parameterTyps.length; i++) {
                parameterTypeStr[i] = parameterTyps[i].getName();
            }
            return parameterTypeStr;
        }
        return null;
    }


    private void initConstants() throws UnknownHostException {
        InitConstants.clientPort = port;
        InitConstants.systemId = id;
        if (!StringUtils.isEmpty(name) && !name.equals("${clientName}")) {
            InitConstants.clientName = name;
        } else {
            InitConstants.clientName = "";
        }
        InitConstants.zookeeperAddress = zookeeperAddress;
    }

    @Value("${redis.host}")
    private String redisHost;
    @Value("${redis.port}")
    private String redisPort;
    @Value("${redis.password}")
    private String redisPassword;
    @Value("${redis.maxIdle}")
    private Integer maxIdle;
    @Value("${redis.maxTotal}")
    private Integer maxTotal;
    @Value("${redis.maxWaitMillis}")
    private Integer maxWaitMills;

    @Value("${systemId}")
    private String id;
    @Value("${port}")
    private Integer port;
    @Value("${clientName}")
    private String name;
    @Value("${zookeeper.address}")
    private String zookeeperAddress;
}
