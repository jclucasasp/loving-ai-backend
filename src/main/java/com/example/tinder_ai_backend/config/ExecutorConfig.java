package com.example.tinder_ai_backend.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
public class ExecutorConfig {

    private final Logger logger = LogManager.getLogger(ExecutorConfig.class);

    @Value("${spring.task.execution.pool.core-size}")
    private int coreSize;

    @Value("${spring.task.execution.pool.max-size}")
    private int maxPool;

    @Value("${spring.task.execution.pool.keep-alive}")
    private int keepAliveSeconds;

    @Value("${spring.task.execution.pool.queue-capacity}")
    private int queueCap;

    @Bean
    public ThreadPoolTaskExecutor chatResponseExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxPool);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setQueueCapacity(queueCap);

        try (ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {
            scheduler.scheduleAtFixedRate(() -> {
                if (executor.getQueueSize() == queueCap) {
                    logger.warn("");
                    executor.setCorePoolSize(executor.getCorePoolSize() * 2);
                    executor.setMaxPoolSize(executor.getMaxPoolSize() * 2);
                }
            }, 1, 10, TimeUnit.SECONDS);
        }

        return executor;
    }
}
