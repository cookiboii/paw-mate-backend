package com.kindtail.adoptmate.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.username:default}")
    private String username;

    @Value("${spring.data.redis.password:#{null}}")
    private String password;

    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean sslEnabled;

    private static final String REDISSON_HOST_PREFIX = "redis://";
    private static final String REDISSON_SSL_HOST_PREFIX = "rediss://";

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String prefix = sslEnabled ? REDISSON_SSL_HOST_PREFIX : REDISSON_HOST_PREFIX;
        String address = prefix + host + ":" + port;

        var singleServerConfig = config.useSingleServer()
                .setAddress(address)
                .setConnectTimeout(10000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);

        if (username != null && !username.isBlank()) {
            singleServerConfig.setUsername(username);
        }
        if (password != null && !password.isBlank()) {
            singleServerConfig.setPassword(password);
        }

        return Redisson.create(config);
    }
}
