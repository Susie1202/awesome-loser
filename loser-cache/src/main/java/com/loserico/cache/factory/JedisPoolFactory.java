package com.loserico.cache.factory;


import com.loserico.cache.config.RedisProperties;
import com.loserico.common.lang.resource.PropertyReader;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.JedisPool;

/**
 * JedisPool工厂类
 * <p>
 * Copyright: Copyright (c) 2019-10-17 14:05
 * <p>
 * Company: Sexy Uncle Inc.
 * <p>
 *
 * @author Rico Yu  ricoyu520@gmail.com
 * @version 1.0
 */
public class JedisPoolFactory implements PoolFactory {

	@Override
	public JedisPool createPool(PropertyReader propertyReader) {
		//是否启用默认配置, 默认Redis为localhost:6379
		boolean defaultPoolEnabled = propertyReader.getBoolean("redis.default.enabled", true);
		if (!defaultPoolEnabled) {
			return null;
		}
		
		//Redis host, 默认localhost
		String host = propertyReader.getString("redis.host", "localhost");
		String overrideHost = System.getProperty("LOSER_REDIS_HOST");
		if (overrideHost != null && !overrideHost.isEmpty()) {
			host = overrideHost;
		} else {
			overrideHost = System.getenv("LOSER_REDIS_HOST");
			if (overrideHost != null && !overrideHost.isEmpty()) {
				host = overrideHost;
			}
		}
		
		//Redis port, 默认6379
		int port = propertyReader.getInt("redis.port", 6379);
		String overridePort = System.getProperty("LOSER_REDIS_PORT");
		if (overridePort != null && !overridePort.isEmpty()) {
			port = Integer.parseInt(overridePort);
		} else {
			overridePort = System.getenv("LOSER_REDIS_PORT");
			if (overridePort != null && !overridePort.isEmpty()) {
				port = Integer.parseInt(overridePort);
			}
		}
		
		//Redis 密码, 默认没有密码
		String password = propertyReader.getString("redis.password");
		String overridePassword = System.getProperty("LOSER_REDIS_PASSWORD");
		if (overridePassword != null && !overridePassword.isEmpty()) {
			password = overridePassword;
		}
		// 默认5秒超时
		int timeout = propertyReader.getInt("redis.timeout", 5000);
		int db = propertyReader.getInt("redis.db", 0);

		if (StringUtils.isNotBlank(password)) {
			return new JedisPool(config(propertyReader), host, port, timeout, password, db);
		} else {
			return new JedisPool(config(propertyReader), host, port, timeout, null, db);
		}
	}

	@Override
	public JedisPool createPool(RedisProperties redisProperties) {
		return new JedisPool(config(redisProperties),
				redisProperties.getHost(),
				redisProperties.getPort(),
				redisProperties.getTimeout(),
				redisProperties.getPassword(),
				redisProperties.getDatabase());
	}

}
