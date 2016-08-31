package com.weimob.jobserver.server.monitor.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.weimob.jobserver.core.job.commond.ClientJobCommond;
import com.weimob.jobserver.core.job.commond.CommondResult;
import com.weimob.jobserver.core.job.dynamic.DynamicJobManager;
import com.weimob.jobserver.core.job.dynamic.ManageResult;
import com.weimob.jobserver.core.reg.constant.JobLockType;
import com.weimob.jobserver.core.reg.constant.JobStatus;
import com.weimob.jobserver.core.reg.constant.JobType;
import com.weimob.jobserver.core.reg.domain.LocalJobConfig;
import com.weimob.jobserver.core.reg.storage.JobNodePath;
import com.weimob.jobserver.core.zk.ZookeeperRegistryCenter;
import com.weimob.jobserver.server.http.HttpClientUtils;
import com.weimob.jobserver.server.job.database.baen.JobConfigEntity;
import com.weimob.jobserver.server.job.database.baen.JobLogBean;
import com.weimob.jobserver.server.job.database.baen.ServerConfigEntity;
import com.weimob.jobserver.server.job.database.constant.ServerStatus;
import com.weimob.jobserver.server.job.database.service.JobConfigService;
import com.weimob.jobserver.server.job.database.service.JobLogInfoService;
import com.weimob.jobserver.server.job.database.service.ServerConfigEntityService;
import com.weimob.jobserver.server.job.util.JobUtil;
import com.weimob.jobserver.server.monitor.constant.MonitorErrorCode;
import com.weimob.jobserver.server.monitor.response.JobListResponse;
import com.weimob.jobserver.server.monitor.response.JsonResponse;
import com.weimob.jobserver.server.monitor.response.LogResponse;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author: kevin
 * @date 2016/08/09.
 */
@Log4j
@Controller
@RequestMapping(value = "monitor")
public class JobManagerController {
    @Autowired
    private ServerConfigEntityService serverConfigService;
    @Autowired
    private JobConfigService jobConfigService;
    @Autowired
    private Scheduler scheduler;
    @Autowired
    private DynamicJobManager jobManager;
    @Autowired
    private ZookeeperRegistryCenter registryCenter;
    @Autowired
    private JobLogInfoService logInfoService;

    @RequestMapping(value = "jobList")
    public String jobList(Model model, JobConfigEntity jobConfig) {
        List<JobConfigEntity> jobConfigs = jobConfigService.findList(jobConfig);
        List<JobListResponse> jobListResponses = new ArrayList<>();
        if (!CollectionUtils.isEmpty(jobConfigs)) {
            for (JobConfigEntity config : jobConfigs) {
                JobListResponse jobListResponse = new JobListResponse();
                BeanUtils.copyProperties(config, jobListResponse);
                jobListResponse.setJobType(jobConfigService.getJobType(JobType.valueOf(config.getJobType())));
                jobListResponse.setLockStratergy(jobConfigService.getLockStratery(JobLockType.valueOf(config.getLockStratergy())));
                Pair<String, Integer> pair = jobConfigService.getJobStatus(JobStatus.valueOf(config.getStatus()));
                jobListResponse.setStatusStr(pair.getLeft());
                jobListResponse.setLevel(pair.getValue());
                Integer serverCount = serverConfigService.countServers(config.getId());
                jobListResponse.setServerCount(serverCount);
                jobListResponses.add(jobListResponse);
            }
        }
        List<String> systemList = jobConfigService.getSystemList(jobConfig);
        model.addAttribute("jobList", jobListResponses);
        model.addAttribute("systemList", systemList);
        model.addAttribute("param", jobConfig);
        return "job_list";
    }

    @RequestMapping(value = "deleteJob", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse deleteJob(@RequestParam("jobId") Integer jobId) {
        JobConfigEntity jobConfigEntity = jobConfigService.get(jobId);
        if (jobConfigEntity == null) {
            return new JsonResponse(MonitorErrorCode.JOB_NOT_EXISTS);
        }
        try {
            JobType jobType = JobType.valueOf(jobConfigEntity.getJobType());
            switch (jobType) {
                case ZK_JOB:
                case HTTP_JOB:
                    jobManager.deleteJob(JobUtil.getDynamicJob(jobConfigEntity));
                    break;
            }
            List<ServerConfigEntity> serverConfigEntities = serverConfigService.findList(new ServerConfigEntity(jobId));
            if (!CollectionUtils.isEmpty(serverConfigEntities)) {
                for (ServerConfigEntity serverConfigEntity : serverConfigEntities) {
                    serverConfigService.delete(serverConfigEntity);
                }
            }
            jobConfigService.delete(jobConfigEntity);
            return new JsonResponse(MonitorErrorCode.SUCESS);
        } catch (Exception e) {
            log.error(String.format("deleteJob error jobId:{%s}", jobId), e);
        }
        return new JsonResponse(MonitorErrorCode.SERVER_ERROR);
    }

    @RequestMapping(value = "pauseJob", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse pauseJob(@RequestParam("jobId") Integer jobId) {
        JobConfigEntity jobConfig = jobConfigService.get(jobId);
        if (jobConfig == null) {
            return new JsonResponse(MonitorErrorCode.JOB_NOT_EXISTS);
        }
        try {
            jobConfig.setStatus(JobStatus.PAUSED.getStatus());
            JobType jobType = JobType.valueOf(jobConfig.getJobType());
            JobNodePath jobNodePath = new JobNodePath(jobConfig.getSystemId(), jobConfig.getJobName());
            switch (jobType) {
                case LOCAL:
                    String zkNodeData = registryCenter.get(jobNodePath.getConfigNodePath());
                    LocalJobConfig localJobConfig = JSON.parseObject(zkNodeData, LocalJobConfig.class);
                    localJobConfig.setJobStatus(JobStatus.PAUSED);
                    registryCenter.persist(jobNodePath.getJobDataPath(), JSON.toJSONString(localJobConfig));
                    break;
                case ZK_JOB:
                    jobManager.pauseJob(new JobKey(jobConfig.getSystemId() + "_" + jobConfig.getJobName(), jobConfig.getGroupName()));
                    jobConfigService.update(jobConfig);
                    break;
                case HTTP_JOB:
                    jobManager.pauseJob(new JobKey(jobConfig.getSystemId() + "_" + jobConfig.getJobName(), jobConfig.getGroupName()));
                    jobConfigService.update(jobConfig);
                    break;
            }
            return new JsonResponse(MonitorErrorCode.SUCESS);
        } catch (Exception e) {
            log.error("pauseJob error,jobdata:" + JSON.toJSONString(jobConfig), e);
        }
        return new JsonResponse(MonitorErrorCode.SERVER_ERROR);
    }

    @RequestMapping(value = "resumeJob", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse resumeJob(@RequestParam("jobId") Integer jobId) {
        JobConfigEntity jobConfig = jobConfigService.get(jobId);
        if (jobConfig == null) {
            return new JsonResponse(MonitorErrorCode.JOB_NOT_EXISTS);
        }
        JobType jobType = JobType.valueOf(jobConfig.getJobType());
        try {
            jobConfig.setStatus(JobStatus.EXECUTING.getStatus());
            JobNodePath jobNodePath = new JobNodePath(jobConfig.getSystemId(), jobConfig.getJobName());
            ManageResult manageResult;
            switch (jobType) {
                case LOCAL:
                    registryCenter.persist(jobNodePath.getConfigNodePath(), ClientJobCommond.RESUME.name());
                    break;
                case ZK_JOB:
                    manageResult = jobManager.resumeJob(new JobKey(jobConfig.getSystemId() + "_" + jobConfig.getJobName()));
                    if (manageResult == ManageResult.NON_EXISTED) {
                        jobManager.createAndRegisterJob(JobUtil.getDynamicJob(jobConfig), true);
                    }
                    registryCenter.persist(jobNodePath.getConfigNodePath(), ClientJobCommond.RESUME.name());
                    jobConfigService.update(jobConfig);
                    break;
                case HTTP_JOB:
                    manageResult = jobManager.resumeJob(new JobKey(jobConfig.getSystemId() + "_" + jobConfig.getJobName()));
                    if (manageResult == ManageResult.NON_EXISTED) {
                        jobManager.createAndRegisterJob(JobUtil.getDynamicJob(jobConfig), true);
                    }
                    jobConfigService.update(jobConfig);
                    break;
            }
            return new JsonResponse(MonitorErrorCode.SUCESS);
        } catch (Exception e) {
            log.error("resume job error,job info:" + JSON.toJSONString(jobConfig), e);
        }
        return new JsonResponse(MonitorErrorCode.SERVER_ERROR);
    }

    @RequestMapping(value = "jobDetail/{id}")
    public String jobDetail(Model model, @PathVariable("id") Integer id) {
        if (id == null || id <= 0) {
            return "job_detail";
        }
        JobConfigEntity jobEntity = jobConfigService.get(id);
        if (jobEntity == null) {
            return "job_detail";
        }
        ServerConfigEntity serverConfigEntity = new ServerConfigEntity(id);
        List<ServerConfigEntity> serverConfigEntities = serverConfigService.findList(serverConfigEntity);
        if (!CollectionUtils.isEmpty(serverConfigEntities)) {
            for (ServerConfigEntity configEntity : serverConfigEntities) {
                if (StringUtils.isEmpty(configEntity.getClientName())) {
                    configEntity.setClientName("无");
                }
            }
        }
        jobEntity.setServerConfigList(serverConfigEntities);
        model.addAttribute("jobDetail", jobEntity);
        return "job_detail";
    }

    @RequestMapping(value = "/edit/{jobId}")
    public String editJob(@PathVariable("jobId") Integer jobId, Model model) {
        JobConfigEntity jobConfig = jobConfigService.get(jobId);
        if (jobConfig == null) {
            model.addAttribute("error", "job不存在");
            return "add_job";
        }
        model.addAttribute("job", jobConfig);
        return "edit_job";
    }

    @RequestMapping(value = "/add")
    public String addJob(HttpServletRequest request) {
        init(request);
        return "add_job";
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse saveJob(JobConfigEntity jobConfig, Model model, HttpServletRequest request) {
        if (jobConfig == null) {
            return new JsonResponse(MonitorErrorCode.JOB_NAME_EMPTY);
        }
        try {
            String clientName = request.getParameter("clientName");
            model.addAttribute("job", jobConfig);
            model.addAttribute("clientName", clientName);
            JobConfigEntity persistJob = jobConfigService.get(jobConfig.getId());
            if (persistJob == null) {
                return new JsonResponse(MonitorErrorCode.JOB_NOT_EXISTS);
            }
            persistJob.setCronExpression(jobConfig.getCronExpression());
            jobConfigService.update(persistJob);
        } catch (Exception e) {
            log.error("saveJob error,data:" + JSON.toJSONString(jobConfig), e);
        }
        return new JsonResponse(MonitorErrorCode.SERVER_ERROR);
    }

    /**
     * 保存http类型的任务
     *
     * @param jobConfig
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "saveJob", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse save(JobConfigEntity jobConfig, Model model, HttpServletRequest request) {
        String clientName = request.getParameter("clientName");
        String ip = request.getParameter("ip");
        String portStr = request.getParameter("port");
        Integer port;
        if (StringUtils.isEmpty(clientName)) {
            clientName = "";
        }
        if (StringUtils.isEmpty(ip) || StringUtils.isEmpty(portStr)) {
            return new JsonResponse(MonitorErrorCode.CLIENT_NAME_EMPTY);
        }
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            return new JsonResponse(MonitorErrorCode.INVALID_PORT);
        }
        StringBuilder urlBuilder = new StringBuilder("http://");
        urlBuilder.append(ip).append(":").append(port).append("/").append(clientName).append("jobserver");
        try {
            String responseJson = HttpClientUtils.doGet(urlBuilder.toString());
            if (StringUtils.isEmpty(responseJson)) {
                return new JsonResponse(MonitorErrorCode.CLIENT_NOT_AVALIABLE);
            }
            CommondResult commondResult = JSONObject.parseObject(responseJson, CommondResult.class);
            if (!commondResult.isSuccess()) {
                return new JsonResponse(MonitorErrorCode.CLIENT_NOT_AVALIABLE);
            }
            jobConfig.setJobClass("");
            JobConfigEntity jobConfigEntity = jobConfigService.get(jobConfig);
            if (jobConfigEntity == null) {
                jobConfig.setJobType(JobType.HTTP_JOB.getType());
                jobConfig.setLockStratergy(JobLockType.OVVERAL_UNIQUE.getType());
                jobConfig.setGroupName("DEFAULT");
                jobConfig.setStatus(JobStatus.PAUSED.getStatus());
                jobConfigService.save(jobConfig);
            } else {
                jobConfigEntity = jobConfigService.get(jobConfig);
                jobConfig.setId(jobConfigEntity.getId());
            }
            ServerConfigEntity param = new ServerConfigEntity();
            param.setJobId(jobConfig.getId());
            param.setServerIp(ip);
            ServerConfigEntity serverConfigEntity = serverConfigService.get(param);
            if (serverConfigEntity == null) {
                param.setClientName(clientName);
                param.setServerPort(port);
                param.setWeight(1);
                param.setServerStatus(ServerStatus.OK.getStatus());
                serverConfigService.save(param);
            } else {
                param.setId(serverConfigEntity.getId());
                serverConfigService.update(param);
            }
            return new JsonResponse(MonitorErrorCode.SUCESS);
        } catch (Exception e) {
            log.error(String.format("save error,data:%s", jobConfig), e);
            return new JsonResponse(MonitorErrorCode.SERVER_ERROR);
        }
    }

    @RequestMapping(value = "monitor")
    public String monitor(Model model, JobLogBean logParam, HttpServletRequest request) {
        String systemId = request.getParameter("systemId");
        String jobName = request.getParameter("jobName");
        List<JobConfigEntity> jobConfigEntities = null;
        Page<JobLogBean> jobLogBeenList = null;
        if (logParam == null) {
            logParam = new JobLogBean();
        }
        if (logParam.getPageSize() == null || logParam.getPageSize() <= 0) {
            logParam.setPageSize(20);
        }
        if (logParam.getPageNum() == null || logParam.getPageNum() <= 0) {
            logParam.setPageNum(0);
        }
        Map<Integer, JobConfigEntity> jobMap = new HashMap<>();
        if (!StringUtils.isEmpty(systemId)) {
            if (!StringUtils.isEmpty(jobName)) {
                JobConfigEntity jobConfigEntity = jobConfigService.get(jobName, systemId);
                model.addAttribute("job", jobConfigEntity);
                logParam.setJobId(jobConfigEntity.getId());
                jobMap.put(jobConfigEntity.getId(), jobConfigEntity);
                jobLogBeenList = logInfoService.findList(logParam);
            } else {
                JobConfigEntity jobConfigEntity = new JobConfigEntity();
                jobConfigEntity.setSystemId(systemId);
                model.addAttribute("job", jobConfigEntity);
                jobConfigEntities = jobConfigService.findList(jobConfigEntity);
                if (!CollectionUtils.isEmpty(jobConfigEntities)) {
                    for (JobConfigEntity configEntity : jobConfigEntities) {
                        jobMap.put(configEntity.getId(), configEntity);
                    }
                    jobLogBeenList = logInfoService.findByJobIds(jobMap.keySet(), logParam);
                }
            }
        } else {
            jobLogBeenList = logInfoService.findList(logParam);
        }
        List<String> systemList = jobConfigService.getSystemList(null);
        List<LogResponse> logList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(jobLogBeenList)) {
            if (CollectionUtils.isEmpty(jobMap)) {
                Set<Integer> jobIdSet = new HashSet<>(jobLogBeenList.size());
                for (JobLogBean jobLogBean : jobLogBeenList) {
                    jobIdSet.add(jobLogBean.getJobId());
                }
                jobConfigEntities = jobConfigService.findByIds(jobIdSet);
                if (!CollectionUtils.isEmpty(jobConfigEntities)) {
                    for (JobConfigEntity configEntity : jobConfigEntities) {
                        jobMap.put(configEntity.getId(), configEntity);
                    }
                }
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (JobLogBean jobLogBean : jobLogBeenList) {
                LogResponse logResponse = new LogResponse();
                JobConfigEntity jobConfigEntity = jobMap.get(jobLogBean.getJobId());
                if (jobConfigEntity == null) {
                    continue;
                }
                logResponse.setSystemId(jobConfigEntity.getSystemId());
                logResponse.setJobName(jobConfigEntity.getJobName());
                logResponse.setEndTime(dateFormat.format(jobLogBean.getEndTime()));
                logResponse.setStartTime(dateFormat.format(jobLogBean.getStartTime()));
                logResponse.setUpdateTime(dateFormat.format(jobLogBean.getUpdateTime()));
                logResponse.setSuccess(jobLogBean.isSuccess());
                logList.add(logResponse);
            }
        }
        if (jobLogBeenList == null) {
            jobLogBeenList = new Page<>();
        }
        int total = 0;
        if (!CollectionUtils.isEmpty(jobLogBeenList)) {
            total = jobLogBeenList.getPages();
        }
        model.addAttribute("logInfo", logList);
        model.addAttribute("systemList", systemList);
        model.addAttribute("pageNum", logParam.getPageNum());
        model.addAttribute("pageSize", logParam.getPageSize());
        model.addAttribute("total", total);
        return "monitor_detail";
    }

    @InitBinder
    private void init(HttpServletRequest request) {
        String path = request.getContextPath();
        String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
        request.setAttribute("base", basePath);
    }
}
