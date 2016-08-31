package com.weimob.jobserver.client.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.weimob.jobserver.client.register.InitConstants;
import com.weimob.jobserver.client.register.JobManagerCenter;
import com.weimob.jobserver.core.job.BaseAbstractJob;
import com.weimob.jobserver.core.job.RemoteJob;
import com.weimob.jobserver.core.job.commond.CommondResult;
import com.weimob.jobserver.core.job.commond.JobCommond;
import com.weimob.jobserver.core.job.log.JobLogInfo;
import com.weimob.jobserver.core.tools.CommonConstants;
import com.weimob.jobserver.core.tools.DateUtil;
import lombok.extern.log4j.Log4j;
import org.springframework.util.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author: kevin
 * @date 2016/07/27.
 */
@Log4j
@WebServlet(name = "jobserver", urlPatterns = "/jobserver")
public class JobServerInitServelet extends HttpServlet {

    public static JobManagerCenter jobManagerCenter;

    /**
     * get方法用于心跳检查
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        responseJson(resp, new CommondResult(true));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(req.getInputStream()));
        StringBuilder inputStr = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            inputStr.append(line);
        }
        if (StringUtils.isEmpty(inputStr)) {
            return;
        }
        JobCommond jobCommond = JSONObject.parseObject(String.valueOf(inputStr), JobCommond.class);
        if (jobCommond == null) {
            return;
        }
        try {
            switch (jobCommond.getCommondType()) {
                case TRIGGER:
                    triggerJob(jobCommond);
                    break;
            }
        } catch (Exception e) {
            log.error(String.format("doPost exception commond[%s]", jobCommond), e);
        }
    }

    /**
     * 执行任务
     *
     * @param jobCommond
     */
    private void triggerJob(final JobCommond jobCommond) {
        String jobName = jobCommond.getJobName();
        BaseAbstractJob abstractJob = jobManagerCenter.getJobServiceStorage().get(jobName);
        if (abstractJob instanceof RemoteJob) {
            final RemoteJob remoteJob = (RemoteJob) abstractJob;
            jobManagerCenter.getTaskExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    JobLogInfo jobLogInfo = new JobLogInfo(InitConstants.systemId, jobCommond.getJobName(), jobCommond.getGroup(), System.currentTimeMillis(), InitConstants.clientIp);
                    jobLogInfo.setClientPort(InitConstants.clientPort);
                    try {
                        remoteJob.execute();
                        jobLogInfo.setSuccess(true);
                    } catch (Exception e) {
                        jobLogInfo.setSuccess(false);
                    } finally {
                        jobLogInfo.setEndTime(System.currentTimeMillis());
                        jobManagerCenter.getRedisClient().lpush(CommonConstants.LOG_REDIS_KEY, JSON.toJSONString(jobLogInfo));
                    }
                }
            });

        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    private void responseJson(HttpServletResponse response, CommondResult result) throws IOException {
        response.setContentType("application/json");
        result.setTimeStamp(DateUtil.currentDateTime());
        response.getWriter().write(JSON.toJSONString(result));
    }
}
