package com.cheter0410.blindspot.client.cache;

import com.mojang.authlib.yggdrasil.response.FriendDto;
import net.minecraft.client.gui.screens.social.PlayerSocialManager.PlayerData;

import java.util.List;

/**
 * The remapped friends list, valid as long as vanilla's friend data still holds the same input list.
 */
public record FriendsCache(List<FriendDto> input, List<PlayerData> output) {
}
