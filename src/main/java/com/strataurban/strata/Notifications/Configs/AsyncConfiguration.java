package com.strataurban.strata.Notifications.Configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Configuration for asynchronous notification processing
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfiguration {

    /**
     * Thread pool executor specifically for notification processing
     * Configured to handle high load while preventing system overload
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size - minimum threads always alive
        executor.setCorePoolSize(5);

        // Max pool size - maximum threads under high load
        executor.setMaxPoolSize(20);

        // Queue capacity - tasks waiting when all threads busy
        executor.setQueueCapacity(500);

        // Thread name prefix for easy identification in logs
        executor.setThreadNamePrefix("notification-async-");

        // Keep-alive time for idle threads above core size
        executor.setKeepAliveSeconds(60);

        // Rejection policy when queue is full
        executor.setRejectedExecutionHandler(new NotificationRejectionHandler());

        // Allow core threads to timeout
        executor.setAllowCoreThreadTimeOut(false);

        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("Notification executor initialized with core={}, max={}, queue={}",
                executor.getCorePoolSize(),
                executor.getMaxPoolSize(),
                executor.getQueueCapacity());

        return executor;
    }

    /**
     * Custom rejection handler for when notification queue is full
     * Logs the rejection and allows graceful degradation
     */
    private static class NotificationRejectionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.error("Notification task rejected - executor queue full. Active threads: {}, Queue size: {}",
                    executor.getActiveCount(),
                    executor.getQueue().size());

            // You could implement fallback logic here:
            // - Store in a dead letter queue
            // - Trigger an alert
            // - Write to a separate log file for retry

            // For now, we just log the error
            // The notification will be lost, but the system won't crash
        }
    }
}