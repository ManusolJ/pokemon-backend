package com.poketeambuilder.infrastructure.interceptors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpRequest;

import org.springframework.http.client.ClientHttpResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;

@RequiredArgsConstructor
public class PokeApiThrottlingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(PokeApiThrottlingInterceptor.class);

    private final long delayMs;

    private long lastRequestTime = 0;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        throttle();

        return execution.execute(request, body);
    }

    private void throttle() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestTime;

        if (lastRequestTime > 0 && elapsed < delayMs) {
            long sleepTime = delayMs - elapsed;

            log.debug("Throttling PokeAPI request for {}ms", sleepTime);

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while throttling PokeAPI requests", e);
            }
        }

        lastRequestTime = System.currentTimeMillis();
    }
}