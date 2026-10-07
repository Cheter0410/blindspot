package com.cheter0410.blindspot.client.mixin;

import com.cheter0410.blindspot.client.cache.TabListCache;
import com.cheter0410.blindspot.client.cache.TabListCacheHolder;
import com.cheter0410.blindspot.client.cache.TabListVersion;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Holds the tab list cache for this connection and invalidates it when the set of listed players changes.
 * Both hooked call sites run after the packet was handed over to the client main thread.
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin implements TabListCacheHolder {

    @Unique
    private TabListCache blindspot$tabListCache;

    @Override
    public TabListCache blindspot$tabListCache() {
        return blindspot$tabListCache;
    }

    @Override
    public void blindspot$setTabListCache(TabListCache cache) {
        blindspot$tabListCache = cache;
    }

    @ModifyExpressionValue(method = "applyPlayerInfoUpdate", at = {
            @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"),
            @At(value = "INVOKE", target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z")
    })
    private boolean blindspot$onListedChanged(boolean changed) {
        if (changed) {
            TabListVersion.increment();
        }
        return changed;
    }

    @ModifyExpressionValue(method = "handlePlayerInfoRemove", at = @At(value = "INVOKE", target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z"))
    private boolean blindspot$onListedRemoved(boolean changed) {
        if (changed) {
            TabListVersion.increment();
        }
        return changed;
    }
}
