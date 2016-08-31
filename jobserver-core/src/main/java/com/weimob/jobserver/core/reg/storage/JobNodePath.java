package com.weimob.jobserver.core.reg.storage;

public final class JobNodePath {

    /**
     * 作业立刻触发节点名称.
     */
    public static final String TRIGGER_NODE = "trigger";

    /**
     * 作业暂停节点名称.
     */
    public static final String PAUSED_NODE = "paused";


    /**
     * 作业关闭节点名称.
     */
    public static final String SHUTDOWN_NODE = "shutdown";

    /**
     * 作业状态节点名称.
     */
    public static final String STATUS_NODE = "status";

    private static final String CONFIG_NODE = "config";

    private static final String SERVERS_NODE = "servers";

    private static final String EXECUTION_NODE = "execution";

    private static final String JOB_DATA = "jobdata";
    private final String jobName;

    public JobNodePath(String systemId, String jobName) {
        this.jobName = systemId + "/" + jobName;
    }

    /**
     * 获取节点全路径.
     *
     * @param node 节点名称
     * @return 节点全路径
     */
    public String getFullPath(final String node) {
        return String.format("/%s/%s", jobName, node);
    }

    /**
     * 获取job的根路径
     *
     * @return
     */
    public String getJobPath() {
        return String.format("/%s", jobName);
    }

    public String getJobDataPath() {
        return String.format("/%s/%s", jobName, JOB_DATA);
    }

    /**
     * 获取配置节点路径.
     *
     * @return 配置节点路径
     */
    public String getConfigNodePath() {
        return String.format("/%s/%s", jobName, CONFIG_NODE);
    }

    /**
     * 获取作业节点IP地址根路径.
     *
     * @return 作业节点IP地址根路径
     */
    public String getServerNodePath() {
        return String.format("/%s/%s", jobName, SERVERS_NODE);
    }

    /**
     * 根据IP地址获取作业节点路径.
     *
     * @param serverIp 作业服务器IP地址
     * @return 作业节点IP地址路径
     */
    public String getServerNodePath(final String serverIp) {
        return String.format("%s/%s", getServerNodePath(), serverIp);
    }

    /**
     * 根据IP地址和子节点名称获取作业节点路径.
     *
     * @param serverIp 作业服务器IP地址
     * @param nodeName 子节点名称
     * @return 作业节点IP地址子节点路径
     */
    public String getServerNodePath(final String serverIp, final String nodeName) {
        return String.format("%s/%s", getServerNodePath(serverIp), nodeName);
    }

    /**
     * 获取运行节点根路径.
     *
     * @return 运行节点根路径
     */
    public String getExecutionNodePath() {
        return String.format("/%s/%s", jobName, EXECUTION_NODE);
    }

    /**
     * 获取运行节点路径.
     *
     * @param nodeName 子节点名称
     * @return 运行节点路径
     */
    public String getExecutionNodePath(final String nodeName) {
        return String.format(getExecutionNodePath() + "/%s", nodeName);
    }

}