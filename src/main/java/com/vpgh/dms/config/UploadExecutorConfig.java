package com.vpgh.dms.config;

import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.Executor;

@Configuration
public class UploadExecutorConfig {

    @Value("${upload.executor.core-size:4}")
    private int coreSize;

    @Value("${upload.executor.max-size:16}")
    private int maxSize;

    @Value("${upload.executor.queue-capacity:100}")
    private int queueCapacity;

    @Bean(name = "uploadExecutor", destroyMethod = "")
    public Executor uploadExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(coreSize);
        exec.setMaxPoolSize(maxSize);
        exec.setQueueCapacity(queueCapacity);
        exec.setThreadNamePrefix("upload-");
        exec.setTaskDecorator(contextPropagatingDecorator());
        exec.initialize();
        return exec;
    }

    private TaskDecorator contextPropagatingDecorator() {
        return runnable -> {
            User capturedUser = SecurityUtil.getCurrentUserFromThreadLocal();
            SecurityContext capturedContext = SecurityContextHolder.getContext();
            return () -> {
                SecurityContext previousContext = SecurityContextHolder.getContext();
                try {
                    if (capturedUser != null) {
                        SecurityUtil.setCurrentUser(capturedUser);
                    }
                    SecurityContextHolder.setContext(capturedContext);
                    runnable.run();
                } finally {
                    SecurityUtil.clear();
                    SecurityContextHolder.setContext(previousContext);
                }
            };
        };
    }
}
