
package com.weimob.jobserver.core.zk;


import com.alibaba.fastjson.JSON;
import com.weimob.jobserver.core.job.listener.ZkDataChangeListener;
import com.weimob.jobserver.core.job.listener.ZkEventListener;
import com.weimob.jobserver.core.reg.exception.RegExceptionHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;

/**
 * 基于Zookeeper的注册中心.
 *
 * @author kevin
 */
@Slf4j
public class ZookeeperRegistryCenter implements CoordinatorRegistryCenter {

    @Getter(AccessLevel.PROTECTED)
    private ZookeeperConfiguration zkConfig;

    private final Map<String, TreeCache> caches = new HashMap<>();

    private static CuratorFramework client;

    public ZookeeperRegistryCenter(final ZookeeperConfiguration zkConfig) {
        this.zkConfig = zkConfig;
    }

    @Override
    public void init() {
        log.info(String.format("job server: zookeeper registry center init, init config: {}.", JSON.toJSONString(zkConfig)));
        if (client != null) {
            if (client.getZookeeperClient().isConnected()) {
                return;
            }
        }
        Builder builder = CuratorFrameworkFactory.builder()
                .connectString(zkConfig.getServerLists())
                .retryPolicy(new ExponentialBackoffRetry(zkConfig.getBaseSleepTimeMilliseconds(), zkConfig.getMaxRetries(), zkConfig.getMaxSleepTimeMilliseconds()))
                .namespace(zkConfig.getNamespace());
        if (0 != zkConfig.getSessionTimeoutMilliseconds()) {
            builder.sessionTimeoutMs(zkConfig.getSessionTimeoutMilliseconds());
        }
        if (0 != zkConfig.getConnectionTimeoutMilliseconds()) {
            builder.connectionTimeoutMs(zkConfig.getConnectionTimeoutMilliseconds());
        }
        if (!StringUtils.isEmpty(zkConfig.getDigest())) {
            builder.authorization("digest", zkConfig.getDigest().getBytes(Charset.forName("UTF-8")))
                    .aclProvider(new ACLProvider() {
                        @Override
                        public List<ACL> getDefaultAcl() {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }

                        @Override
                        public List<ACL> getAclForPath(final String path) {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }
                    });
        }
        client = builder.build();
        client.start();
        try {
            client.blockUntilConnected(zkConfig.getMaxSleepTimeMilliseconds() * zkConfig.getMaxRetries(), TimeUnit.MILLISECONDS);
            if (!client.getZookeeperClient().isConnected()) {
                throw new KeeperException.OperationTimeoutException();
            }
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }


    @Override
    public void close() {
        for (Entry<String, TreeCache> each : caches.entrySet()) {
            each.getValue().close();
        }
        CloseableUtils.closeQuietly(client);
    }


    @Override
    public String get(final String key) {
        TreeCache cache = findTreeCache(key);
        if (null == cache) {
            return getDirectly(key);
        }
        ChildData resultInCache = cache.getCurrentData(key);
        if (null != resultInCache) {
            return null == resultInCache.getData() ? null : new String(resultInCache.getData(), Charset.forName("UTF-8"));
        }
        return getDirectly(key);
    }

    private TreeCache findTreeCache(final String key) {
        for (Entry<String, TreeCache> entry : caches.entrySet()) {
            if (key.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public String getDirectly(final String key) {
        try {
            return new String(client.getData().forPath(key), Charset.forName("UTF-8"));
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
            return null;
        }
    }

    public List<String> getChildrenKeys(final String key) {
        try {
            List<String> result = client.getChildren().forPath(key);
            Collections.sort(result, new Comparator<String>() {

                @Override
                public int compare(final String o1, final String o2) {
                    return o2.compareTo(o1);
                }
            });
            return result;
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isExisted(final String key) {
        try {
            if (StringUtils.isEmpty(key)) {
                return false;
            }
            if (!key.startsWith("/")) {
                return false;
            }
            return null != client.checkExists().forPath(key);
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
            return false;
        }
    }

    @Override
    public void persist(final String key, final String value) {
        try {
            if (!isExisted(key)) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(key, value.getBytes());
            } else {
                update(key, value);
            }
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }

    @Override
    public void update(final String key, final String value) {
        try {
            client.inTransaction().check().forPath(key).and().setData().forPath(key, value.getBytes(Charset.forName("UTF-8"))).and().commit();
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }

    @Override
    public void persistEphemeral(final String key, final String value) {
        try {
            if (isExisted(key)) {
                client.delete().deletingChildrenIfNeeded().forPath(key);
            }
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(key, value.getBytes(Charset.forName("UTF-8")));
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }

    @Override
    public String persistSequential(final String key) {
        try {
            return client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(key);
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
        return null;
    }

    @Override
    public void persistEphemeralSequential(final String key) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(key);
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }

    @Override
    public void remove(final String key) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(key);
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }


    @Override
    public CuratorFramework getRawClient() {
        return client;
    }

    @Override
    public void addCacheData(final String cachePath) {
        TreeCache cache = new TreeCache(client, cachePath);
        try {
            cache.start();
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
        caches.put(cachePath + "/", cache);
    }

    @Override
    public TreeCache getRawCache(final String cachePath) {
        return caches.get(cachePath + "/");
    }

    public void addChildWatcher(final String path, final ZkEventListener zkEventListener) throws Exception {
        final PathChildrenCache pc = new PathChildrenCache(client, path, true);
        pc.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        pc.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                Type eventType = event.getType();
                ChildData childData = event.getData();
                zkEventListener.eventHandle(eventType, childData, path);
            }
        });
    }

    public void addNodeCacheWatcher(final String path, final ZkDataChangeListener zkEventListener) throws Exception {
        final NodeCache nodeCache = new NodeCache(client, path, false);
        nodeCache.start();
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                ChildData childData = nodeCache.getCurrentData();
                if (childData == null) {
                    return;
                }
                //获取当前节点的最近数据
                byte[] bytes = childData.getData();
                zkEventListener.eventHandle(new String(bytes), path);
            }
        });
    }
}
