package com.cheter0410.blindspot.client.cache;

/**
 * Implemented on {@link net.minecraft.world.scores.Scoreboard} via mixin.
 * <p>
 * The team version counter increases whenever team membership, which feeds the tab list ordering, may
 * have changed. It is per instance, so the integrated server's scoreboard (server thread) never touches
 * the client scoreboard's counter. Score changes are tracked per objective, see
 * {@link ObjectiveCacheHolder}.
 */
public interface ScoreboardCacheHolder {

    /** Increases on any change to team membership. */
    int blindspot$teamsVersion();

    SidebarCache blindspot$sidebarCache();

    void blindspot$setSidebarCache(SidebarCache cache);
}
