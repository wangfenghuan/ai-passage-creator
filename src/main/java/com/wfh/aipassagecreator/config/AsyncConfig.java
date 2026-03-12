package com.wfh.aipassagecreator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Title: AsyncConfig
 * @Author wangfenghuan
 * @Package com.wfh.aipassagecreator.config
 * @Date 2026/3/12 20:31
 * @description:
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "articleExector")
    public Executor articleExector(){
        ThreadPoolTaskExecutor threadPoolExecutor = new ThreadPoolTaskExecutor();
        threadPoolExecutor.setCorePoolSize(5);
        threadPoolExecutor.setMaxPoolSize(10);
        threadPoolExecutor.setQueueCapacity(100);
        threadPoolExecutor.setThreadNamePrefix("article-async-");
        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolExecutor.setAwaitTerminationSeconds(60);
        threadPoolExecutor.initialize();
        return threadPoolExecutor;
    }
}
