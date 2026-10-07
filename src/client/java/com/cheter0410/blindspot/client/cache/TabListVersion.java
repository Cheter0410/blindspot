package com.cheter0410.blindspot.client.cache;

/**
 * Version counter for the tab list sort inputs held by {@link net.minecraft.client.multiplayer.PlayerInfo}
 * and the set of listed players: game mode, list order and listing changes.
 * <p>
 * Only accessed from the client main thread (packet handling and rendering both run there).
 */
public final class TabListVersion {

    private static int version;

    private TabListVersion() {
    }

    public static int get() {
        return version;
    }

    public static void increment() {
        version++;
    }
}
