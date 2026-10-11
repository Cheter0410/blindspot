package com.cheter0410.blindspot.client.mixin;

import com.cheter0410.blindspot.client.cache.ObjectiveCacheHolder;
import com.cheter0410.blindspot.client.cache.ScoreboardCacheHolder;
import com.cheter0410.blindspot.client.cache.SidebarCache;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tracks changes to scores (per objective, see {@link ObjectiveCacheHolder}) and team membership as
 * version counters. Every hook runs at HEAD so the counter also moves when a method returns early or
 * throws; an unnecessary increment only costs one cache rebuild, never correctness.
 */
@Mixin(Scoreboard.class)
public class ScoreboardMixin implements ScoreboardCacheHolder {

    @Unique
    private int blindspot$teamsVersion;

    @Unique
    private SidebarCache blindspot$sidebarCache;

    @Override
    public int blindspot$teamsVersion() {
        return blindspot$teamsVersion;
    }

    @Override
    public SidebarCache blindspot$sidebarCache() {
        return blindspot$sidebarCache;
    }

    @Override
    public void blindspot$setSidebarCache(SidebarCache cache) {
        blindspot$sidebarCache = cache;
    }

    @Unique
    private static void blindspot$scoresChanged(Objective objective) {
        ((ObjectiveCacheHolder) objective).blindspot$incrementScoresVersion();
    }

    // Creates scores (value 0) without any notification; the two-argument overload delegates here.
    @Inject(method = "getOrCreatePlayerScore(Lnet/minecraft/world/scores/ScoreHolder;Lnet/minecraft/world/scores/Objective;Z)Lnet/minecraft/world/scores/ScoreAccess;", at = @At("HEAD"))
    private void blindspot$onGetOrCreateScore(ScoreHolder holder, Objective objective, boolean forceWritable, CallbackInfoReturnable<?> cir) {
        blindspot$scoresChanged(objective);
    }

    // The only notification for in-place value, display and number format changes through ScoreAccess.
    @Inject(method = "onScoreChanged", at = @At("HEAD"))
    private void blindspot$onScoreChanged(ScoreHolder holder, Objective objective, Score score, CallbackInfo ci) {
        blindspot$scoresChanged(objective);
    }

    // Only the objectives this holder has a score in are affected. Resets are rare, so the map copy
    // listPlayerScores makes is fine.
    @Inject(method = "resetAllPlayerScores", at = @At("HEAD"))
    private void blindspot$onResetAllScores(ScoreHolder holder, CallbackInfo ci) {
        for (Objective objective : ((Scoreboard) (Object) this).listPlayerScores(holder).keySet()) {
            blindspot$scoresChanged(objective);
        }
    }

    @Inject(method = "resetSinglePlayerScore", at = @At("HEAD"))
    private void blindspot$onResetSingleScore(ScoreHolder holder, Objective objective, CallbackInfo ci) {
        blindspot$scoresChanged(objective);
    }

    @Inject(method = "removeObjective", at = @At("HEAD"))
    private void blindspot$onRemoveObjective(Objective objective, CallbackInfo ci) {
        blindspot$scoresChanged(objective);
    }

    @Inject(method = "addPlayerToTeam", at = @At("HEAD"))
    private void blindspot$onAddPlayerToTeam(String player, PlayerTeam team, CallbackInfoReturnable<Boolean> cir) {
        blindspot$teamsVersion++;
    }

    // The single-argument overload delegates here.
    @Inject(method = "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V", at = @At("HEAD"))
    private void blindspot$onRemovePlayerFromTeam(String player, PlayerTeam team, CallbackInfo ci) {
        blindspot$teamsVersion++;
    }

    @Inject(method = "removePlayerTeam", at = @At("HEAD"))
    private void blindspot$onRemovePlayerTeam(PlayerTeam team, CallbackInfo ci) {
        blindspot$teamsVersion++;
    }
}
