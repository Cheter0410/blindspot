package com.cheter0410.blindspot.client.cache;

/**
 * Implemented on {@link net.minecraft.world.level.chunk.LevelChunk} via mixin.
 * <p>
 * The version increases whenever block or fluid states in the chunk may have changed: single block
 * changes and full chunk data from the server. Unloading is not counted, because the client drops the
 * chunk object then and a reloaded chunk is a different instance.
 */
public interface ChunkBlockVersion {

    int blindspot$blockVersion();
}
