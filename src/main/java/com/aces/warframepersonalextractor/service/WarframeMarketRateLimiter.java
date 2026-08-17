package com.aces.warframepersonalextractor.service;

import org.springframework.stereotype.Service;

@Service
public class WarframeMarketRateLimiter {

    /*
     * Warframe.Market limit:
     * 3 requests / second.
     *
     * 400ms gives us some breathing room:
     * ~2.5 requests / second.
     */
    private static final long MIN_REQUEST_INTERVAL_MS = 400;

    private long lastRequestTime = 0;

    public synchronized void waitForRequestSlot() {

        long now = System.currentTimeMillis();

        long elapsed =
                now - lastRequestTime;

        long waitTime =
                MIN_REQUEST_INTERVAL_MS - elapsed;

        if (waitTime > 0) {

            try {

                Thread.sleep(waitTime);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                throw new IllegalStateException(
                        "Warframe.Market request limiter was interrupted.",
                        e
                );
            }
        }

        lastRequestTime =
                System.currentTimeMillis();
    }
}