package com.cheter0410.blindspot.client.cache;

import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

/**
 * A boat's last computed status, together with everything it was computed from: the bounding box and
 * the chunks around it.
 * <p>
 * Vanilla's status checks only read block and fluid states within one block of the bounding box. The
 * cache covers one more block on each side, so it doesn't depend on that exact extent. It stays valid
 * as long as the box is equal and every one of these chunks is still the same loaded instance with the
 * same {@link ChunkBlockVersion}.
 */
public final class BoatStatusCache {

    // Vanilla reads up to one block outside the bounding box, this adds a second as a safety margin.
    private static final int MARGIN = 2;

    private final BoatStatus result;
    private final AABB box;
    private final LevelChunk[] chunks;
    private final int[] versions;

    private BoatStatusCache(BoatStatus result, AABB box, LevelChunk[] chunks, int[] versions) {
        this.result = result;
        this.box = box;
        this.chunks = chunks;
        this.versions = versions;
    }

    public static BoatStatusCache create(Level level, AABB box, BoatStatus result) {
        int minX = minChunk(box.minX), maxX = maxChunk(box.maxX);
        int minZ = minChunk(box.minZ), maxZ = maxChunk(box.maxZ);
        int count = (maxX - minX + 1) * (maxZ - minZ + 1);
        LevelChunk[] chunks = new LevelChunk[count];
        int[] versions = new int[count];

        int i = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                LevelChunk chunk = level.getChunk(x, z);
                chunks[i] = chunk;
                versions[i] = ((ChunkBlockVersion) chunk).blindspot$blockVersion();
                i++;
            }
        }
        return new BoatStatusCache(result, box, chunks, versions);
    }

    public BoatStatus result() {
        return result;
    }

    public boolean isValidFor(Level level, AABB box) {
        if (!this.box.equals(box)) {
            return false;
        }

        // An equal box covers the same chunk range, visited in the same order as in create().
        int minX = minChunk(box.minX), maxX = maxChunk(box.maxX);
        int minZ = minChunk(box.minZ), maxZ = maxChunk(box.maxZ);
        int i = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                // Unloaded chunks resolve to the client's shared empty chunk, so loading or unloading one also fails here.
                LevelChunk chunk = level.getChunk(x, z);
                if (chunk != chunks[i] || ((ChunkBlockVersion) chunk).blindspot$blockVersion() != versions[i]) {
                    return false;
                }
                i++;
            }
        }
        return true;
    }

    private static int minChunk(double min) {
        return SectionPos.blockToSectionCoord(Mth.floor(min) - MARGIN);
    }

    private static int maxChunk(double max) {
        return SectionPos.blockToSectionCoord(Mth.ceil(max) + MARGIN);
    }

    /**
     * The result of {@code AbstractBoat.getStatus()} including its side effects. {@code landFriction} is
     * only written by vanilla for {@link AbstractBoat.Status#ON_LAND} and is 0 otherwise.
     */
    public record BoatStatus(AbstractBoat.Status status, double waterLevel, float landFriction) {
    }
}
