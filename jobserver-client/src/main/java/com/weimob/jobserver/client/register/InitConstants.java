package com.weimob.jobserver.client.register;

import com.weimob.jobserver.core.interval.LocalHostService;

import java.io.Serializable;

/**
 * @author: kevin
 * @date 2016/08/04.
 */

public class InitConstants implements Serializable {
    public static String systemId;
    public static Integer clientPort;
    public static String clientIp = new LocalHostService().getIp();
    public static String clientName;
    public static String zookeeperAddress;
    public static final String DEFAULT_NAMESPACE = "job-server";
}
