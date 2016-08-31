package com.weimob.jobserver.core.job.dynamic;

import lombok.extern.log4j.Log4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author: kevin
 * @date 2016/08/09.
 */
@Log4j
public class DynamicJobManager {
    private Scheduler jobScheduler;

    public DynamicJobManager(Scheduler jobScheduler) {
        this.jobScheduler = jobScheduler;
    }

    /**
     * 动态注册任务
     *
     * @param dynamicJob
     * @throws ParseException
     * @throws SchedulerException
     */
    public void createAndRegisterJob(DynamicJob dynamicJob) throws ParseException, SchedulerException {
        final TriggerKey triggerKey = dynamicJob.triggerKey();
        if (jobScheduler.checkExists(triggerKey)) {
            final Trigger trigger = jobScheduler.getTrigger(triggerKey);
            throw new SchedulerException("Already exist trigger [" + trigger + "] by key [" + triggerKey + "] in Scheduler");
        }
        register(dynamicJob);
    }

    private void register(DynamicJob dynamicJob) throws SchedulerException {
        final TriggerKey triggerKey = dynamicJob.triggerKey();
        final CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(dynamicJob.cronExpression());
        final CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey)
                .withSchedule(cronScheduleBuilder)
                .build();
        final JobDetail jobDetail = dynamicJob.jobDetail();
        final Date date = jobScheduler.scheduleJob(jobDetail, cronTrigger);
        log.info(String.format("Register DynamicJob {} on [{}]", dynamicJob, date));
    }

    /**
     * 注册动态任务
     *
     * @param dynamicJob
     * @param autoDelete true 如果已经存在则删除之前的，重新注册
     * @throws ParseException
     * @throws SchedulerException
     */
    public void createAndRegisterJob(DynamicJob dynamicJob, boolean autoDelete) throws ParseException, SchedulerException {
        if (!autoDelete) {
            createAndRegisterJob(dynamicJob);
            return;
        } else {
            final TriggerKey triggerKey = dynamicJob.triggerKey();
            if (jobScheduler.checkExists(triggerKey)) {
                deleteJob(dynamicJob);
            }
        }
        register(dynamicJob);
    }

    /**
     * 获取trigger列表
     *
     * @return
     */
    public List<String> getTriggerList() {
        List<String> triggerNames;
        try {
            GroupMatcher<TriggerKey> matcher = GroupMatcher.anyTriggerGroup();
            Set<TriggerKey> triggerKeys = jobScheduler.getTriggerKeys(matcher);
            if (triggerKeys != null || triggerKeys.size() > 0) {
                triggerNames = new ArrayList<>(triggerKeys.size());
                for (TriggerKey triggerKey : triggerKeys) {
                    triggerNames.add(triggerKey.getName());
                }
            }
        } catch (SchedulerException e) {
            log.error("Failed to get trigger list.", e);
        }
        return new ArrayList<>(0);
    }

    /**
     * 执行任务
     *
     * @param dynamicJob
     * @return
     * @throws SchedulerException
     */
    public ManageResult triggerJob(DynamicJob dynamicJob) throws SchedulerException {
        if (!hasJob(dynamicJob.jobDetail().getKey())) return ManageResult.NON_EXISTED;
        jobScheduler.triggerJob(new JobKey(dynamicJob.jobName(), Scheduler.DEFAULT_GROUP));
        return ManageResult.RUNNING;
    }

    /**
     * 改变cron表达式
     *
     * @param dynamicJob
     * @param cronExpression
     * @return
     * @throws ParseException
     * @throws SchedulerException
     */
    public ManageResult changeCronExpression(DynamicJob dynamicJob, String cronExpression) throws ParseException, SchedulerException {
        List<? extends Trigger> triggers = getJobTriggers(dynamicJob.jobDetail().getKey());
        for (Trigger trigger : triggers) {
            if (trigger instanceof CronTrigger) {
                CronTrigger cronTrigger = (CronTrigger) trigger;
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
                TriggerKey triggerKey = TriggerKey.triggerKey(dynamicJob.jobName(), trigger.getJobKey().getGroup());
                cronTrigger = cronTrigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
                jobScheduler.rescheduleJob(triggerKey, cronTrigger);
            }
        }
        return ManageResult.CRON_CHANGED;
    }

    public JobDetail getJobDetail(String jobName) throws SchedulerException {
        return jobScheduler.getJobDetail(new JobKey(jobName, Scheduler.DEFAULT_GROUP));
    }

    public List<? extends Trigger> getJobTriggers(JobKey jobKey) throws SchedulerException {
        if (!hasJob(jobKey)) return null;
        return jobScheduler.getTriggersOfJob(jobKey);
    }


    /**
     * 中断指定的任务
     *
     * @param jobKey
     * @return
     * @throws SchedulerException
     */
    public ManageResult interruptJob(JobKey jobKey) throws SchedulerException {
        if (!hasJob(jobKey)) return ManageResult.NON_EXISTED;
        jobScheduler.interrupt(jobKey);
        return ManageResult.INTERRUPTED;
    }

    public boolean hasJob(JobKey jobKey) throws SchedulerException {
        return jobScheduler.checkExists(jobKey);
    }

    public ManageResult resumeJob(JobKey jobKey) throws SchedulerException {
        if (!hasJob(jobKey)) return ManageResult.NON_EXISTED;
        jobScheduler.resumeJob(jobKey);
        return ManageResult.RESUMED;
    }

    public ManageResult pauseJob(JobKey jobKey) throws SchedulerException {
        if (!hasJob(jobKey)) return ManageResult.NON_EXISTED;
        jobScheduler.pauseJob(jobKey);
        return ManageResult.PAUSED;
    }

    /**
     * 删除job
     *
     * @param dynamicJob
     * @return
     * @throws SchedulerException
     */
    public boolean deleteJob(DynamicJob dynamicJob) throws SchedulerException {
        JobKey jobKey = dynamicJob.jobDetail().getKey();
        if (hasJob(jobKey)) {
            TriggerKey triggerKey = dynamicJob.triggerKey();
            jobScheduler.pauseTrigger(triggerKey);
            jobScheduler.unscheduleJob(triggerKey);
            jobScheduler.deleteJob(jobKey);
            return true;
        }
        return false;
    }
}
