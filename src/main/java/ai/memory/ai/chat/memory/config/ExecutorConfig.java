package ai.memory.ai.chat.memory.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
@Configuration
public class ExecutorConfig {

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

        try {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                if (executor.getQueueSize() > (queueCap - 10)) {
                    log.warn("Queue size reached or exceeded. Increasing thread core and pool size by a factor of 2!");
                    executor.setCorePoolSize(executor.getCorePoolSize() * 2);
                    executor.setMaxPoolSize(executor.getMaxPoolSize() * 2);
                }
            }, 1, 10, TimeUnit.SECONDS);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return executor;
    }
}
