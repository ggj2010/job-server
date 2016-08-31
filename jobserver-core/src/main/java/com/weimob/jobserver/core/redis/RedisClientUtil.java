package com.weimob.jobserver.core.redis;

import lombok.Setter;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <li>redis工具类</li>
 *
 * @author 贺康雷
 * @version 1.0
 * @since 2015年10月14日上午9:25:17
 **/
public class RedisClientUtil {
    /**
     * 连接池配置信息
     */
    private JedisPoolConfig config;
    /**
     * 连接池
     */
    private ShardedJedisPool shardedJedisPool;
    /**
     * redis服务端IP
     */
    @Setter
    private String ip;
    /**
     * redis password
     */
    @Setter
    private String password;
    /**
     * redis服务端端口
     */
    @Setter
    private Integer port;
    /**
     * 空闲最大连接数
     */
    @Setter
    private Integer maxIdle;
    /**
     * 最大连接数
     */
    @Setter
    private Integer maxTotal;
    /**
     * 连接最大等待时间单位：毫秒
     */
    @Setter
    private Integer maxWaitMillis;

    public void initRedis() {
        initJedisPoolConfig();
        initialPool();
    }

    public RedisClientUtil() {
    }


    /**
     * 生成连接池配置信息
     */
    private void initJedisPoolConfig() {
        config = new JedisPoolConfig();
        config.setMaxIdle(maxIdle);
        config.setMaxTotal(maxTotal);
        config.setMaxWaitMillis(maxWaitMillis);
    }

    /**
     * 初始化切片池
     */
    private void initialPool() {
        // slave链接
        List<JedisShardInfo> shards = new ArrayList<>();
        JedisShardInfo jedisShardInfo = new JedisShardInfo(ip, port, "master");
        jedisShardInfo.setPassword(password);

        shards.add(jedisShardInfo);
        // 构造池
        shardedJedisPool = new ShardedJedisPool(config, shards);

    }

    /**
     * @param key     存储key
     * @param value   存储数据
     * @param seconds 有效时间 单位秒
     */
    public void set(String key, String value, int seconds) {
        ShardedJedis client = null;
        try {
            client = shardedJedisPool.getResource();
            if (-1 == seconds) {
                client.set(key, value);
            } else {
                client.setex(key, seconds, value);
            }
        } catch (JedisConnectionException jedisE) {
            throw jedisE;
        } catch (Exception e) {
            throw e;
        } finally {
            /** 业务执行完毕，将连接返回给连接池 */
            if (null != client) {
                shardedJedisPool.returnResourceObject(client);
            }
        }

    }

    /**
     * @param key   存储数据key
     * @param value 存储数据
     */
    public void set(String key, String value) {
        ShardedJedis client = null;
        try {
            client = shardedJedisPool.getResource();
            client.set(key, value);
        } catch (JedisConnectionException jedisE) {
            throw jedisE;
        } catch (Exception e) {
            throw e;
        } finally {
            /** 业务执行完毕，将连接返回给连接池 */
            if (null != client) {
                shardedJedisPool.returnResourceObject(client);
            }
        }
    }

    /**
     * 根据存储key获取相应的存储value数据
     *
     * @param key 存储数据key
     */
    public String get(String key) {
        ShardedJedis client = null;
        try {
            client = shardedJedisPool.getResource();
            return client.get(key);
        } catch (JedisConnectionException jedisE) {
            throw jedisE;
        } catch (Exception e) {
            throw e;
        } finally {
            /** 业务执行完毕，将连接返回给连接池 */
            if (null != client) {
                shardedJedisPool.returnResourceObject(client);
            }
        }
    }

    /**
     * 根据存储key获取相应的存储value数据
     *
     * @param keyList 存储数据key
     */
    public Map<String, String> get(List<String> keyList) {
        ShardedJedis client = null;
        Map<String, String> resultMap = new HashMap<>();
        try {
            client = shardedJedisPool.getResource();
            ShardedJedisPipeline pipeline = client.pipelined();
            for (String key : keyList) {
                pipeline.get(key);
            }
            List<Object> results = pipeline.syncAndReturnAll();
            if (results != null) {
                int index = 0;
                for (Object object : results) {
                    if (object != null) {
                        resultMap.put(keyList.get(index), results.get(index).toString());
                    }
                    index++;
                }
            }
        } catch (JedisConnectionException jedisE) {
            throw jedisE;
        } catch (Exception e) {
            throw e;
        } finally {
            /** 业务执行完毕，将连接返回给连接池 */
            if (null != client) {
                shardedJedisPool.returnResourceObject(client);
            }
        }
        return resultMap;
    }

    public Long incrBy(String key, long addValue, int seconds) {
        Long result = null;
        ShardedJedis client = null;
        try {
            client = shardedJedisPool.getResource();
            ShardedJedisPipeline pipeline = client.pipelined();
            pipeline.incrBy(key, addValue);
            pipeline.expire(key, seconds);
            List<Object> results = pipeline.syncAndReturnAll();
            if (results != null) {
                result = Long.parseLong(results.get(0).toString());
            }
        } catch (JedisConnectionException jedisE) {
            throw jedisE;
        } catch (Exception e) {
            throw e;
        } finally {
            shardedJedisPool.returnResourceObject(client);
        }
        return result;
    }

    /**
     * 根据存储key删除相应的数据
     *
     * @param key 存储数据key
     */
    public void del(String key) {
        ShardedJedis client = null;
        try {
            client = shardedJedisPool.getResource();
            client.del(key);
        } catch (JedisConnectionException jedisE) {
            throw jedisE;
        } catch (Exception e) {
            throw e;
        } finally {
            /** 业务执行完毕，将连接返回给连接池 */
            if (null != client) {
                shardedJedisPool.returnResourceObject(client);
            }
        }
    }

    /**
     * @param keyList  key数组
     * @param period   时间段，单位秒
     * @param unixTime 时间戳，单位毫秒
     * @param trackId  UUID
     */
    public void delAndAddRiskLevelRequest(List<String> keyList, int period, Long unixTime, String trackId) {
        ShardedJedis client = null;
        Long max = unixTime - period * 1000 - 1;
        try {
            client = shardedJedisPool.getResource();
            ShardedJedisPipeline pipeline = client.pipelined();
            for (String key : keyList) {
                /** 删除zSet中score < regiterTime+period 的记录 */
                pipeline.zremrangeByScore(key, 1, max);
                /** zSet添加记录, score=regiterTime,member=trackId*/
                pipeline.zadd(key, unixTime, trackId);
            }
            pipeline.syncAndReturnAll();
        } catch (JedisConnectionException jedisE) {
            throw jedisE;
        } catch (Exception e) {
            throw e;
        } finally {
            /** 业务执行完毕，将连接返回给连接池 */
            if (null != client) {
                shardedJedisPool.returnResourceObject(client);
            }
        }
    }

    /**
     * 统计zSet，时间段内的请求数
     *
     * @param key    redis key
     * @param period 统计时间段
     */
    public Long getZSetCount(String key, int period, Long unixTime) {
        Long count;
        ShardedJedis client = null;
        Long min = unixTime - period * 1000;
        try {
            client = shardedJedisPool.getResource();
            count = client.zcount(key, min, unixTime);
            return count;
        } catch (JedisConnectionException jedisE) {
            throw jedisE;
        } catch (Exception e) {
            throw e;
        } finally {
            /** 业务执行完毕，将连接返回给连接池 */
            if (null != client) {
                shardedJedisPool.returnResourceObject(client);
            }
        }
    }

    /**
     * 取截止当前，某时间段内的zSet交集
     *
     * @param keyList key数组
     * @param period  统计时间段
     */
    public Long getZinterStore(List<String> keyList, int period, Long unixTime) {
        Long count;
        ShardedJedis client = null;
        Long min = unixTime - period * 1000;
        try {
            client = shardedJedisPool.getResource();
            client.zrangeByScore(keyList.get(0), unixTime - period * 1000, unixTime);
            Jedis jedis = client.getShard(keyList.get(0).getBytes());
            jedis.zinterstore("getZinterStoreTmp", keyList.get(0), keyList.get(1));
            count = client.zcount("getZinterStoreTmp", min * keyList.size(), unixTime * keyList.size());
            client.del("getZinterStoreTmp");
            return count;
        } catch (JedisConnectionException jedisE) {
            throw jedisE;
        } catch (Exception e) {
            throw e;
        } finally {
            /** 业务执行完毕，将连接返回给连接池 */
            if (null != client) {
                shardedJedisPool.returnResourceObject(client);
            }
        }
    }

    /**
     * 将value存入到key的list的head中
     *
     * @param key   redis的key
     * @param value 存入的value
     */
    public void lpush(String key, String value) {
        ShardedJedis client = null;
        try {
            client = shardedJedisPool.getResource();
            client.lpush(key, value);
        } finally {
            shardedJedisPool.returnResourceObject(client);
        }
    }

    /**
     * 从redis的list的tail中pop值
     *
     * @param key redis的key
     * @return pop出来的值，空为NULL
     */
    public String rpop(String key) {
        ShardedJedis client = null;
        try {
            client = shardedJedisPool.getResource();
            return client.rpop(key);
        } finally {
            shardedJedisPool.returnResourceObject(client);
        }
    }

    /**
     * 从连接池中获取连接
     */
    public ShardedJedis getResource() {
        return shardedJedisPool.getResource();
    }

    /**
     * 将连接返回连接池
     *
     * @param client 连接
     */
    public void returnResourceObject(ShardedJedis client) {
        shardedJedisPool.returnResourceObject(client);
    }
}
