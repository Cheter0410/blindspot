package com.cheter0410.blindspot.client.cache;

import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;

import java.util.List;

/**
 * The filtered, sorted and limited sidebar entries for one objective, valid as long as that objective's
 * score version is unchanged. Teams don't affect which entries are shown or their order, only how they
 * are formatted afterwards, so team changes don't invalidate it.
 */
public record SidebarCache(Objective objective, int scoresVersion, long limit, List<PlayerScoreEntry> entries) {

    public boolean isValidFor(Objective objective, int scoresVersion) {
        return this.objective == objective && this.scoresVersion == scoresVersion;
    }
}
