package com.cheter0410.blindspot.client.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Objects;

/**
 * Optional self-check for the caches, enabled with {@code -Dblindspot.verifyCaches=true}.
 * <p>
 * On every cache hit the vanilla result is computed as well and compared with the cached one.
 * Mismatches are logged, plus a periodic summary with the hit rate. When disabled, callers skip it
 * behind the {@link #ENABLED} constant, so it costs nothing.
 */
public final class CacheVerification {

    public static final boolean ENABLED = Boolean.getBoolean("blindspot.verifyCaches");

    private static final Logger LOGGER = LoggerFactory.getLogger("blindspot");
    private static final int SUMMARY_INTERVAL = 1000;
    private static final int MAX_LOGGED_MISMATCHES = 20;

    private final String name;
    private long hits;
    private long misses;
    private long mismatches;

    public CacheVerification(String name) {
        this.name = name;
        LOGGER.info("[Blindspot] Cache verification enabled for {}", name);
    }

    public void hit(Object expected, Object cached) {
        hits++;
        if (!Objects.equals(expected, cached)) {
            mismatches++;
            if (mismatches <= MAX_LOGGED_MISMATCHES) {
                LOGGER.error("[Blindspot] {} cache mismatch #{}: expected {} but cached {}", name, mismatches, expected, cached);
            }
        }
        logSummaryIfDue();
    }

    public void miss() {
        misses++;
        logSummaryIfDue();
    }

    private void logSummaryIfDue() {
        long total = hits + misses;
        if (total % SUMMARY_INTERVAL == 0) {
            LOGGER.info("[Blindspot] {} cache: {} hits, {} misses ({}% hit rate), {} mismatches",
                    name, hits, misses, String.format(Locale.ROOT, "%.1f", 100.0 * hits / total), mismatches);
        }
    }
}
