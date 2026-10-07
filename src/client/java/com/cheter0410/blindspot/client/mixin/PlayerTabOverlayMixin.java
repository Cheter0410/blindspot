package com.cheter0410.blindspot.client.mixin;

import com.cheter0410.blindspot.client.cache.CacheVerification;
import com.cheter0410.blindspot.client.cache.ScoreboardCacheHolder;
import com.cheter0410.blindspot.client.cache.TabListCache;
import com.cheter0410.blindspot.client.cache.TabListCacheHolder;
import com.cheter0410.blindspot.client.cache.TabListVersion;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

/**
 * Caches the sorted tab list order. Vanilla re-sorts every listed player every frame while the tab list
 * is open, although the sort only depends on list order, spectator mode, team name and profile name.
 * The cache is invalidated whenever one of those can change (see {@link TabListVersion} and the
 * scoreboard's team version). The rows themselves (names, ping, scores, hearts) are still built by
 * vanilla every frame.
 */
@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private static final CacheVerification blindspot$tabListVerification =
            CacheVerification.ENABLED ? new CacheVerification("Tab list") : null;

    @WrapMethod(method = "getPlayerInfos")
    private List<PlayerInfo> blindspot$useCachedPlayerInfos(Operation<List<PlayerInfo>> original) {
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) {
            return original.call();
        }

        TabListCacheHolder holder = (TabListCacheHolder) player.connection;
        // PlayerInfo.getTeam() resolves teams through the level's scoreboard, so key on exactly that one.
        Scoreboard scoreboard = level.getScoreboard();
        int teamsVersion = ((ScoreboardCacheHolder) scoreboard).blindspot$teamsVersion();
        int playerInfoVersion = TabListVersion.get();

        TabListCache cache = holder.blindspot$tabListCache();
        if (cache != null && cache.isValidFor(scoreboard, teamsVersion, playerInfoVersion)) {
            if (CacheVerification.ENABLED) {
                blindspot$tabListVerification.hit(original.call(), cache.players());
            }
            return cache.players();
        }

        if (CacheVerification.ENABLED) {
            blindspot$tabListVerification.miss();
        }

        // Vanilla returns Stream.toList(), which is already unmodifiable and safe to share.
        List<PlayerInfo> players = original.call();
        holder.blindspot$setTabListCache(new TabListCache(scoreboard, teamsVersion, playerInfoVersion, players));
        return players;
    }
}
