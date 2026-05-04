package com.poketeambuilder.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
    
    @Bean
    CacheManager cacheManager(){
        SimpleCacheManager manager = new SimpleCacheManager();

        manager.setCaches(List.of(
            buildCache("types", 20, 24, TimeUnit.HOURS),
            buildCache("items", 500, 12, TimeUnit.HOURS),
            buildCache("natures", 30, 24, TimeUnit.HOURS),
            buildCache("teams", 0, 0, null),
            buildCache("moves", 1_000, 12, TimeUnit.HOURS),
            buildCache("pokemon", 1_500, 6, TimeUnit.HOURS),
            buildCache("species", 1_200, 6, TimeUnit.HOURS),
            buildCache("abilities", 350, 24, TimeUnit.HOURS),
            buildCache("typeEffectiveness", 400, 24, TimeUnit.HOURS)
        ));

        return manager;
    }

    private CaffeineCache buildCache(String name, int maxSize, long duration, TimeUnit unit) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .recordStats()
                .maximumSize(maxSize)
                .expireAfterWrite(duration, unit)
                .build());
    }
}
