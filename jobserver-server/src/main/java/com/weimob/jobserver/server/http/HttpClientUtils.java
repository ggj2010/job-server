package com.weimob.jobserver.server.http;

import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j;

import org.apache.commons.collections.MapUtils;
import org.springframework.http.HttpEntity;

import java.util.Map;

@Log4j
public class HttpClientUtils {

    /**
     * post请求
     *
     * @param url
     * @param jsonParam
     * @return
     */
    public static String doPost(String url, Map<String, String> jsonParam) {
        if (MapUtils.isEmpty(jsonParam)) {
            return doPost(url);
        }
        try {
            String json = JSON.toJSONString(jsonParam);
            return RestClient.getClient().postForObject(url, json, String.class);
        } catch (Exception e) {
        }

        return "";
    }

    public static String doPost(String url, String jsonStr) {
        return RestClient.getClient().postForObject(url, jsonStr, String.class);
    }

    /**
     * post请求
     *
     * @param url
     * @return
     */
    public static String doPost(String url) {
        try {
            return RestClient.getClient().postForObject(url, HttpEntity.EMPTY, String.class);
        } catch (Exception e) {
            log.error(String.format("POST请求出错：{}", url), e);
        }

        return "";
    }

    /**
     * get请求
     *
     * @param url
     * @return
     */
    public static String doGet(String url) {
        return RestClient.getClient().getForObject(url, String.class);
    }

}