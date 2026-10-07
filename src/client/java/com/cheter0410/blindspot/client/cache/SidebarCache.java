package com.cheter0410.blindspot.client.cache;

import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;

import java.util.List;

/**
 * The filtered, sorted and limited sidebar entries for one objective, valid as long as the owning
 * scoreboard's versions are unchanged.
 */
public record SidebarCache(Objective objective, int scoresVersion, int teamsVersion, long limit, List<PlayerScoreEntry> entries) {

    public boolean isValidFor(Objective objective, int scoresVersion, int teamsVersion) {
        return this.objective == objective && this.scoresVersion == scoresVersion && this.teamsVersion == teamsVersion;
    }
}
