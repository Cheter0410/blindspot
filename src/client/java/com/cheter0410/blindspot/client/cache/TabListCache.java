package com.cheter0410.blindspot.client.cache;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.scores.Scoreboard;

import java.util.List;

/**
 * The sorted tab list player order, valid as long as none of the sort inputs changed.
 */
public record TabListCache(Scoreboard scoreboard, int teamsVersion, int playerInfoVersion, List<PlayerInfo> players) {

    public boolean isValidFor(Scoreboard scoreboard, int teamsVersion, int playerInfoVersion) {
        return this.scoreboard == scoreboard && this.teamsVersion == teamsVersion && this.playerInfoVersion == playerInfoVersion;
    }
}
