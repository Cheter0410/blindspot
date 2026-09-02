package com.cheter0410.blindspot.client.mixin;

import net.minecraft.client.gui.screens.social.PresenceHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PresenceHandler.class)
public class PresenceHandlerMixin {

    @Unique
    private int blindspot$tickCounter = 0;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void blindspot$throttleTick(CallbackInfo ci) {
        blindspot$tickCounter++;

        if (blindspot$tickCounter < 100) {
            ci.cancel();
            return;
        }

        blindspot$tickCounter = 0;
    }
}
