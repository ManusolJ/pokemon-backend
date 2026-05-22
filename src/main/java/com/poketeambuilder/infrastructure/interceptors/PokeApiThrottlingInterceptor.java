package com.poketeambuilder.infrastructure.interceptors;

import java.io.IOException;

import org.springframework.http.HttpRequest;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import lombok.extern.slf4j.Slf4j;

/**
 * Caps the outbound rate to PokeAPI to a minimum spacing of {@link #delayMs} between
 * consecutive requests.
 *
 * <p>The interceptor stamps the future release time inside the lock so concurrent
 * threads queue behind one another and each leaves the lock exactly {@link #delayMs}
 * milliseconds after the previous one. Throughput is therefore deterministically capped at
 * {@code 1000 / delayMs} requests per second regardless of caller concurrency.</p>
 */
@Slf4j
public class PokeApiThrottlingInterceptor implements ClientHttpRequestInterceptor {

    private final long delayMs;

    private long nextAllowedRequestTime = 0;
    private final Object lock = new Object();

    public PokeApiThrottlingInterceptor(long delayMs) {
        this.delayMs = delayMs;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        throttle();
        return execution.execute(request, body);
    }

    /**
     * Atomically computes the wait needed to respect the rate cap, stamps the future release
     * time, and then sleeps  so other callers can queue.
     */
    private void throttle() {
        long sleepTime;

        synchronized (lock) {
            long now = System.currentTimeMillis();
            sleepTime = Math.max(0, nextAllowedRequestTime - now);
            nextAllowedRequestTime = Math.max(now, nextAllowedRequestTime) + delayMs;
        }

        if (sleepTime <= 0) {
            return;
        }

        log.debug("Throttling PokeAPI request for {}ms", sleepTime);

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while throttling PokeAPI requests", e);
        }
    }
}
