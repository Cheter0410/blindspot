package com.cheter0410.blindspot.client.mixin;

import com.cheter0410.blindspot.client.cache.CacheVerification;
import com.cheter0410.blindspot.client.cache.ScoreboardCacheHolder;
import com.cheter0410.blindspot.client.cache.SidebarCache;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Caches the sidebar's {@code listPlayerScores -> filter -> sorted -> limit} result.
 * <p>
 * On a hit, the cached entries (already filtered, sorted and limited) are fed into vanilla's pipeline in
 * place of the full score list. Filtering, stable sorting and limiting an already processed list leaves
 * it unchanged, so the outcome is identical while the walk over all score holders and the full sort are
 * skipped. Everything after {@code limit} (team formatting, number formatting, text widths) still runs
 * every frame, so fonts, languages and team styles behave exactly as in vanilla.
 * <p>
 * On a miss, vanilla's own pipeline (including any changes other mods make to it) produces the result,
 * which is captured at {@code limit}.
 */
@Mixin(Hud.class)
public class HudMixin {

    @Shadow
    @Final
    private static Comparator<PlayerScoreEntry> SCORE_DISPLAY_ORDER;

    @Unique
    private static final CacheVerification blindspot$sidebarVerification =
            CacheVerification.ENABLED ? new CacheVerification("Sidebar") : null;

    // Per-call state handed from the listPlayerScores wrapper to the limit wrapper of the same call.
    @Unique
    private Scoreboard blindspot$scoreboard;
    @Unique
    private Objective blindspot$objective;
    @Unique
    private int blindspot$scoresVersion;
    @Unique
    private int blindspot$teamsVersion;
    @Unique
    private SidebarCache blindspot$hit;

    @WrapOperation(method = "displayScoreboardSidebar", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;listPlayerScores(Lnet/minecraft/world/scores/Objective;)Ljava/util/Collection;"))
    private Collection<PlayerScoreEntry> blindspot$useCachedScores(Scoreboard scoreboard, Objective objective, Operation<Collection<PlayerScoreEntry>> original) {
        ScoreboardCacheHolder holder = (ScoreboardCacheHolder) scoreboard;
        int scoresVersion = holder.blindspot$scoresVersion();
        int teamsVersion = holder.blindspot$teamsVersion();
        SidebarCache cache = holder.blindspot$sidebarCache();

        blindspot$scoreboard = scoreboard;
        blindspot$objective = objective;
        blindspot$scoresVersion = scoresVersion;
        blindspot$teamsVersion = teamsVersion;

        if (cache != null && cache.isValidFor(objective, scoresVersion, teamsVersion)) {
            blindspot$hit = cache;
            return cache.entries();
        }

        blindspot$hit = null;
        return original.call(scoreboard, objective);
    }

    @WrapOperation(method = "displayScoreboardSidebar", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;limit(J)Ljava/util/stream/Stream;"))
    private Stream<PlayerScoreEntry> blindspot$captureScores(Stream<PlayerScoreEntry> stream, long limit, Operation<Stream<PlayerScoreEntry>> original) {
        Scoreboard scoreboard = blindspot$scoreboard;
        Objective objective = blindspot$objective;
        SidebarCache hit = blindspot$hit;
        // Drop the references right away so no scoreboard is kept alive after leaving a world.
        blindspot$scoreboard = null;
        blindspot$objective = null;
        blindspot$hit = null;

        if (scoreboard == null) {
            // The listPlayerScores wrapper did not run for this call (e.g. another mod skipped it).
            return original.call(stream, limit);
        }

        if (hit != null && hit.limit() == limit) {
            if (CacheVerification.ENABLED) {
                List<PlayerScoreEntry> actual = original.call(stream, limit).toList();
                blindspot$sidebarVerification.hit(blindspot$vanillaEntries(scoreboard, objective, limit, original), actual);
                return actual.stream();
            }
            return original.call(stream, limit);
        }

        List<PlayerScoreEntry> entries = hit == null
                ? original.call(stream, limit).toList()
                // The limit changed at runtime (only possible through another mod), so the cached list may
                // be too short: rebuild it from the full score list.
                : blindspot$vanillaEntries(scoreboard, objective, limit, original);

        if (CacheVerification.ENABLED) {
            blindspot$sidebarVerification.miss();
        }
        ((ScoreboardCacheHolder) scoreboard).blindspot$setSidebarCache(
                new SidebarCache(objective, blindspot$scoresVersion, blindspot$teamsVersion, limit, entries));
        return entries.stream();
    }

    @Unique
    private static List<PlayerScoreEntry> blindspot$vanillaEntries(Scoreboard scoreboard, Objective objective, long limit, Operation<Stream<PlayerScoreEntry>> limitOperation) {
        Stream<PlayerScoreEntry> sorted = scoreboard.listPlayerScores(objective).stream()
                .filter(entry -> !entry.isHidden())
                .sorted(SCORE_DISPLAY_ORDER);
        return limitOperation.call(sorted, limit).toList();
    }
}
