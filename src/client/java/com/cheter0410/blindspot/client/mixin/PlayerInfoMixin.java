package com.cheter0410.blindspot.client.mixin;

import com.cheter0410.blindspot.client.cache.TabListVersion;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Invalidates the tab list order when a sort input stored on {@link PlayerInfo} actually changes.
 */
@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {

    @Shadow
    private GameType gameMode;

    @Shadow
    private int tabListOrder;

    @Inject(method = "setGameMode", at = @At("HEAD"))
    private void blindspot$onSetGameMode(GameType gameMode, CallbackInfo ci) {
        if (this.gameMode != gameMode) {
            TabListVersion.increment();
        }
    }

    @Inject(method = "setTabListOrder", at = @At("HEAD"))
    private void blindspot$onSetTabListOrder(int tabListOrder, CallbackInfo ci) {
        if (this.tabListOrder != tabListOrder) {
            TabListVersion.increment();
        }
    }
}
