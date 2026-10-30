package com.poketeambuilder.configuration;

import org.springframework.context.annotation.Configuration;

import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Turns on {@code @Async}. Methods run on Boot's {@code applicationTaskExecutor}, which uses
 * virtual threads since {@code spring.threads.virtual.enabled} is set.
 */
@EnableAsync
@Configuration
public class AsyncConfig {
    
}
