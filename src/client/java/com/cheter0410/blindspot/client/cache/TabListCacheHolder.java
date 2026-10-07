package com.cheter0410.blindspot.client.cache;

/**
 * Implemented on {@link net.minecraft.client.multiplayer.ClientPacketListener} via mixin, so the cached
 * tab list lives exactly as long as the connection it was built from.
 */
public interface TabListCacheHolder {

    TabListCache blindspot$tabListCache();

    void blindspot$setTabListCache(TabListCache cache);
}
