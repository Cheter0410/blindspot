package com.cheter0410.blindspot.client.cache;

/**
 * Implemented on {@link net.minecraft.world.scores.Scoreboard} via mixin.
 * <p>
 * The version counters increase whenever data that feeds the sidebar or tab list ordering may have
 * changed. They are per instance, so the integrated server's scoreboard (server thread) never touches
 * the client scoreboard's counters.
 */
public interface ScoreboardCacheHolder {

    /** Increases on any change to scores (creation, value, display, number format, removal). */
    int blindspot$scoresVersion();

    /** Increases on any change to team membership. */
    int blindspot$teamsVersion();

    SidebarCache blindspot$sidebarCache();

    void blindspot$setSidebarCache(SidebarCache cache);
}
