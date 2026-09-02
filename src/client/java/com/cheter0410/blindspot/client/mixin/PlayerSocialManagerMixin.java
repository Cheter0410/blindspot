package com.cheter0410.blindspot.client.mixin;

import com.mojang.authlib.yggdrasil.response.FriendDto;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.gui.screens.social.PlayerSocialManager.PlayerData;
import net.minecraft.client.gui.screens.social.RemoteFriendListUpdateHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlayerSocialManager.class)
public class PlayerSocialManagerMixin {

    @Shadow
    @Final
    private RemoteFriendListUpdateHandler remoteFriendListUpdateHandler;

    @Unique
    private List<FriendDto> blindspot$cachedInput;

    @Unique
    private List<PlayerData> blindspot$cachedOutput;

    @Inject(method = "getFriends", at = @At("HEAD"), cancellable = true)
    private void blindspot$useCacheIfUnchanged(CallbackInfoReturnable<List<PlayerData>> cir) {
        List<FriendDto> current = this.remoteFriendListUpdateHandler.getLatestFriendData().friends();

        if (current == blindspot$cachedInput) {
            cir.setReturnValue(blindspot$cachedOutput);
        }
    }

    @Inject(method = "getFriends", at = @At("RETURN"))
    private void blindspot$updatedCache(CallbackInfoReturnable<List<PlayerData>> cir) {
        blindspot$cachedInput = this.remoteFriendListUpdateHandler.getLatestFriendData().friends();
        blindspot$cachedOutput = cir.getReturnValue();
    }
}
