package com.cheter0410.blindspot.client.mixin;

import com.cheter0410.blindspot.client.cache.FriendsCache;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.services.response.FriendDto;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.gui.screens.social.PlayerSocialManager.PlayerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * Caches the result of {@code getFriends()}. The friend data is replaced (not mutated) by the
 * "Friends List" thread on every successful update, so the input list's identity tells whether the
 * cached output is still valid.
 * <p>
 * The wrapper receives exactly the list vanilla read, so input and output always belong together even
 * when the background thread swaps the data mid-call. Both are stored in one immutable record so a
 * reader on another thread never sees a mismatched pair.
 */
@Mixin(PlayerSocialManager.class)
public class PlayerSocialManagerMixin {

    @Unique
    private volatile FriendsCache blindspot$friendsCache;

    @WrapOperation(method = "getFriends", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/social/PlayerSocialManager;remap(Ljava/util/List;)Ljava/util/List;"))
    private List<PlayerData> blindspot$useCachedFriends(List<FriendDto> friends, Operation<List<PlayerData>> original) {
        FriendsCache cache = blindspot$friendsCache;
        if (cache != null && cache.input() == friends) {
            return cache.output();
        }

        // Vanilla returns Stream.toList(), which is already unmodifiable and safe to share.
        List<PlayerData> output = original.call(friends);
        blindspot$friendsCache = new FriendsCache(friends, output);
        return output;
    }
}
