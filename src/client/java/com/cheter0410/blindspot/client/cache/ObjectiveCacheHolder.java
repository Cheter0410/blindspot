package com.cheter0410.blindspot.client.cache;

/**
 * Implemented on {@link net.minecraft.world.scores.Objective} via mixin.
 * <p>
 * The version counter increases whenever a score of this objective may have changed, so the sidebar
 * only rebuilds when the displayed objective changed, not when any other objective on the scoreboard
 * did. Objectives belong to exactly one scoreboard, so the integrated server's objectives (server
 * thread) never touch the client's counters.
 */
public interface ObjectiveCacheHolder {

    /** Increases on any change to this objective's scores (creation, value, display, number format, removal). */
    int blindspot$scoresVersion();

    void blindspot$incrementScoresVersion();
}
