//package com.jyw.csp.service.redis;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.data.redis.connection.RedisSentinelConfiguration;
//import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//import org.springframework.stereotype.Service;
//
//import com.jyw.csp.controllers.BaseController;
//
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.JedisPoolConfig;
//import redis.clients.jedis.JedisSentinelPool;
//import redis.clients.jedis.params.SetParams;
//
//
//@Service("sentryRedis")
//public class RedisSentryServiceImpl implements RedisService {
//
//	private static final Logger LOGGER = LoggerFactory.getLogger(RedisService.class);
//	
//	private JedisSentinelPool poolConfig = null;
//
//    @Value("${spring.redis.sentinel.nodes}")
//    private String nodes;
//
//    @Value("${spring.redis.jedis.pool.min-idle}")
//    private int minIdle;
//
//    @Value("${spring.redis.jedis.pool.max-idle}")
//    private int maxIdle;
//
//    @Value("${spring.redis.jedis.pool.max-active}")
//    private int maxTotal;
//
//    @Value("${spring.redis.jedis.pool.max-wait}")
//    private int maxWait;
//
//    @Value("${spring.redis.sentinel.master}")
//    private String masterName;
//
//    @Value("${spring.redis.timeout}")
//    private int timeOut;
//    //过期时间
//    @Value("${redis.key.expire}")
//    private int expireTime;
//    //密码
//    @Value("${redis.passwd}")
//    private String passwd;
//    
//    @Bean("redisTemplate")
//    public RedisTemplate<String, Object> getRedisTemplate() {
//    	try {
//	    	Set<String> sentinels = new HashSet<>();
//	        String[] nodeList = nodes.split(",");
//	        for (String node : nodeList) {
//	            sentinels.add(node);
//	        }
//	    	RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration(masterName, sentinels);
//	
//	    	JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
//	        jedisPoolConfig.setMinIdle(minIdle);
//	        jedisPoolConfig.setMaxTotal(maxTotal);
//	        jedisPoolConfig.setMaxIdle(maxIdle);
//	        jedisPoolConfig.setMaxWaitMillis(maxWait);
//	        
//	        JedisConnectionFactory factory = new JedisConnectionFactory(redisSentinelConfiguration, jedisPoolConfig); // 建立Redis的连接
//	        factory.setPassword(passwd);
//	        factory.setTimeout(timeOut);
//	        factory.setPoolConfig(jedisPoolConfig);
//	        
//	        factory.afterPropertiesSet(); // 初始化连接池配置
//	        
//	        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
//	        redisTemplate.setConnectionFactory(factory);
//	        redisTemplate.setKeySerializer(new StringRedisSerializer()); // key的序列化类型
//	        redisTemplate.setValueSerializer(new StringRedisSerializer()); // value的序列化类型
//	        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer()); // value的序列化类型
//	        return redisTemplate;
//    	} catch(Exception e) {
//    		LOGGER.error("getRedisTemplate", e);
//    		return null;
//    	}
//    }
//
//    /**
//     * 初始化连接池
//     *
//     * @return
//     */
//    private JedisSentinelPool getJedisSentinelPool() {
//        if (poolConfig == null || poolConfig.isClosed()) {
//            Set<String> sentinels = new HashSet<>();
//            String[] nodeList = nodes.split(",");
//            for (String node : nodeList) {
//                sentinels.add(node);
//            }
//            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
//            jedisPoolConfig.setMinIdle(minIdle);
//            jedisPoolConfig.setMaxTotal(maxTotal);
//            jedisPoolConfig.setMaxIdle(maxIdle);
//            jedisPoolConfig.setMaxWaitMillis(maxWait);
//            poolConfig = new JedisSentinelPool(masterName, sentinels, jedisPoolConfig, timeOut, passwd);
//        }
//        return poolConfig;
//    }
//
//    /**
//     * 获取 jdeis 客户端
//     *
//     * @return
//     */
//    @Bean
//    private Jedis getJedisClient() {
//        // 查看当前获得的 master 的ip和 端口
//        if (poolConfig != null)
//            LOGGER.debug("-----获取当前可用的 master ip：port----->" + poolConfig.getCurrentHostMaster() + "-----");
//
//        // 从哨兵里面 获取链接
//        return getJedisSentinelPool().getResource();
//    }
//
//    public void closeJedis(Jedis jedis) {
//        try {
//            if (jedis != null) {  
//                   jedis.close();
//            }
//        } catch (Exception e) {
//        	BaseController.RedisActive = false;
//            closeBrokenResource(jedis);
//        }
//    }
//
//    /**
//     * Return jedis connection to the pool, call different return methods depends on whether the connection is broken
//     */
//    public void closeBrokenResource(Jedis jedis) {
//        try {
//            poolConfig.returnBrokenResource(jedis);
//        } catch (Exception e) {
//        	BaseController.RedisActive = false;
//            destroyJedis(jedis);
//        }
//    }
//
//    /**
//     * 在 Jedis Pool 以外强行销毁 Jedis
//     */
//    public static void destroyJedis(Jedis jedis) {
//        if (jedis != null) {
//            try {
//                jedis.quit();
//            } catch (Exception e) {
//                LOGGER.error(">>> RedisUtil-jedis.quit() : " , e);
//                //e.printStackTrace();
//            }
//
//            try {
//                jedis.disconnect();
//            } catch (Exception e) {
//            	BaseController.RedisActive = false;
//                LOGGER.error(">>> RedisUtil-jedis.disconnect() : " , e);
//                //e.printStackTrace();
//            }
//        }
//    }
//
//    @Override
//    public String setRedis(String key, String value) {
//        Jedis client = null;
//        String d = null;
//        try {
//        	long start=System.currentTimeMillis();
//        	client = getJedisClient();
//            //先获取是否存在
//        	//String cacheValue = getRedis(key);
////            if (cacheValue == null) {
////                SetParams params = SetParams.setParams();
////                params.pxAt(expireTime); 
////                params.nx(); // 强制覆盖
//				// NX是不存在时才set， XX是存在时才set， EX是秒，PX是毫秒
//            	//d=client.set("test", value, params);
//        		//将键 key 的值设置为 value ， 并将键 key 的生存时间设置为 毫秒 秒钟。
//        		//如果键 key 已经存在， 那么PSETEX 命令将覆盖已有的值。
//            	d=client.psetex(key, expireTime, value);
//            	//d = client.set(key, value, "NX", "PX", expireTime);
////            } else {
////                SetParams params = SetParams.setParams();
////                params.pxAt(expireTime);
////            	params.xx();
////            	//d = client.set(key, cacheValue, "XX", "PX", expireTime);
////            	d=client.set(key, cacheValue, params);
////            }
//            // 连接是OK的 返回到连接池里面
//            //poolConfig.returnResourceObject(client);
//            long end=System.currentTimeMillis();
//            LOGGER.debug("setRedisCost:" +  (end-start));
//            BaseController.RedisActive = true;
//        } catch (Exception e) {
//        	BaseController.RedisActive = false;
//            LOGGER.error("[存 redis数据] redis 连接池异常，错误信息为：" + e.getMessage());
//            LOGGER.error("setRedis", e);
//        } finally {
//            closeJedis(client);
//        }
//        return d;
//    }
//
//    @Override
//    public String setByPerm(String key, String value) {
//        Jedis client = null;
//        String d = null;
//        try {
//        	client = getJedisClient();
//            d = client.set(key,value);
//            // 连接是OK的 返回到连接池里面
//            //poolConfig.returnResourceObject(client);
//            BaseController.RedisActive = true;
//        } catch (Exception e) {
//        	BaseController.RedisActive = false;
//            LOGGER.error("[存redis数据] redis 连接池异常，错误信息为：" + e.getMessage());
//            LOGGER.error("setByPerm", e);
//        } finally {
//            closeJedis(client);
//        }
//        return d;
//    }
//
//    @Override
//    public String getRedis(String key) {
//        Jedis client = null;
//        String d = null;
//        try {
//        	long start=System.currentTimeMillis();
//        	client = getJedisClient();
//            d = client.get(key);
//            // 连接是OK的 返回到连接池里面
//            //poolConfig.returnResourceObject(client);
//            long end=System.currentTimeMillis();
//            LOGGER.debug("getRedisCost:" +  (end-start));
//            BaseController.RedisActive = true;
//        } catch (Exception e) {
//        	BaseController.RedisActive = false;
//            LOGGER.error("[取redis数据] redis 连接池异常，错误信息为：" + e.getMessage());
//            LOGGER.error("getRedis", e);
//        } finally {
//            closeJedis(client);
//        }
//        return d;
//    }
//
//    public void delRedis(String key) {
//        Jedis client = null;
//        try {
//        	long start=System.currentTimeMillis();
//        	client = getJedisClient();
//            client.del(key);
//            // 连接是OK的 返回到连接池里面
//            //poolConfig.returnResourceObject(client);
//            long end=System.currentTimeMillis();
//    		LOGGER.debug("deleteRedisCost:" +  (end-start));
//    		BaseController.RedisActive = true;
//        } catch (Exception e) {
//        	BaseController.RedisActive = false;
//            LOGGER.error("[取redis数据] redis 连接池异常，错误信息为：" + e.getMessage());
//            LOGGER.error("delRedis", e);
//        } finally {
//            closeJedis(client);
//        }
//    }
//
//}


