package com.poketeambuilder.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Registers {@link TransactionTemplate} beans for services that need to scope a transaction
 * around part of a method body, typically because the rest of the method does I/O that
 * shouldn't hold a DB connection (seed services calling PokeAPI, password reset sending email, etc).
 */
@Configuration
public class TransactionConfig {

    /**
     * Default transaction template with {@code PROPAGATION_REQUIRED}. Joins an outer
     * transaction if one exists; otherwise starts a new one.
     */
    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    /**
     * Transaction template configured with {@code PROPAGATION_REQUIRES_NEW}. Always starts a
     * fresh transaction independent of any outer one. Used by audit / seed-log writes that
     * must commit even when the surrounding business transaction rolls back.
     */
    @Bean
    TransactionTemplate requiresNewTransactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template;
    }
}
