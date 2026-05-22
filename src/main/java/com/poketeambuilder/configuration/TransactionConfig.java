package com.poketeambuilder.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Registers a {@link TransactionTemplate} bean so services that need to scope a transaction
 * around part of a method body can inject it.
 */
@Configuration
public class TransactionConfig {

    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
