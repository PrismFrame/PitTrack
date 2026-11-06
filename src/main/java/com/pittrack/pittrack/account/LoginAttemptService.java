package com.pittrack.pittrack.account;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Service
public class LoginAttemptService {
    private static final int MAX_IP_ATTEMPTS = 30;
    private static final int MAX_TEAM_ATTEMPTS = 20;

    private static final Duration RATE_WINDOW =
        Duration.ofMinutes(15);

    private static final Duration PAIR_WINDOW =
        Duration.ofMinutes(30);

    private final Cache<String, AttemptWindow> ipAttempts =
        Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(30))
            .maximumSize(100_000)
            .build();

    private final Cache<String, AttemptWindow> teamAttempts =
        Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(30))
            .maximumSize(100_000)
            .build();

    private final Cache<String, AttemptWindow> pairAttempts =
        Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofHours(1))
            .maximumSize(100_000)
            .build();

    /**
     * Records a failed authentication attempt.
     */
    public void recordFailure(String ip, String teamNumber) {
        ipAttempts.get(ip, key -> new AttemptWindow())
            .recordFailure(RATE_WINDOW);

        teamAttempts.get(teamNumber, key -> new AttemptWindow())
            .recordFailure(RATE_WINDOW);

        pairAttempts.get(createPairKey(ip, teamNumber),
                key -> new AttemptWindow())
            .recordFailure(PAIR_WINDOW);
    }

    /**
     * Checks whether an IP or team has exceeded its limit.
     *
     * Returns the number of seconds until another attempt
     * should be allowed, or zero if no restriction applies.
     * 
     * @param ip the IP address of the client.
     * @param teamNumber the team number of the user.
     */
    public long getRetryAfter(String ip, String teamNumber) {
        long ipWait = ipAttempts.getIfPresent(ip) == null
            ? 0
            : ipAttempts.getIfPresent(ip)
                .getRateLimitWait(MAX_IP_ATTEMPTS, RATE_WINDOW);

        long teamWait = teamAttempts.getIfPresent(teamNumber) == null
            ? 0
            : teamAttempts.getIfPresent(teamNumber)
                .getRateLimitWait(MAX_TEAM_ATTEMPTS, RATE_WINDOW);

        long pairWait = pairAttempts.getIfPresent(
                createPairKey(ip, teamNumber)) == null
            ? 0
            : pairAttempts.getIfPresent(
                createPairKey(ip, teamNumber))
                .getProgressiveWait();

        return Math.max(
            Math.max(ipWait, teamWait),
            pairWait
        );
    }

    /**
     * Clears the IP/team combination after successful login.
     * 
     * @param ip the IP address of the client.
     * @param teamNumber the team number of the user.
     */
    public void recordSuccess(String ip, String teamNumber) {
        pairAttempts.invalidate(createPairKey(ip, teamNumber));
    }

    private String createPairKey(String ip, String teamNumber) {
        return ip + ":" + teamNumber;
    }

    private static class AttemptWindow {
        private int attempts;
        private Instant windowStart = Instant.now();
        private Instant lastFailure = Instant.now();

        /** Records a failed authentication attempt.
         * 
         * @param window the time window for rate limiting.
         */
        public synchronized void recordFailure(Duration window) {
            Instant now = Instant.now();

            if (now.isAfter(windowStart.plus(window))) {
                attempts = 0;
                windowStart = now;
            }

            attempts++;
            lastFailure = now;
        }

        /** Returns the number of seconds to wait before allowing another attempt.
         * 
         * @param maximumAttempts the maximum number of attempts allowed.
         * @param window the time window for rate limiting.
         * @return the number of seconds to wait.
         */
        public synchronized long getRateLimitWait(int maximumAttempts, Duration window) {
            Instant now = Instant.now();

            if (now.isAfter(windowStart.plus(window))) {
                attempts = 0;
                windowStart = now;
                return 0;
            }

            if (attempts < maximumAttempts) {
                return 0;
            }

            return Math.max(
                1,
                Duration.between(
                    now,
                    windowStart.plus(window)
                ).toSeconds()
            );
        }

        /** Returns the number of seconds to wait before allowing another attempt.
         * 
         * @return the number of seconds to wait.
         */
        public synchronized long getProgressiveWait() {
            if (attempts < 4) {
                return 0;
            }

            // 4th failure: 1 second
            // 5th failure: 2 seconds
            // 6th failure: 4 seconds
            // 7th failure: 8 seconds
            // Maximum: 30 seconds
            long delay = Math.min(
                30,
                1L << Math.min(attempts - 4, 5)
            );

            long elapsed = Duration.between(
                lastFailure,
                Instant.now()
            ).toSeconds();

            return Math.max(0, delay - elapsed);
        }
    }
}
