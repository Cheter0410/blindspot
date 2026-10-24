package com.cheter0410.blindspot.client.mixin;

import com.cheter0410.blindspot.client.cache.ChunkBlockVersion;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Counts changes to the chunk's block and fluid states. On the client every block change goes through
 * {@code setBlockState} (server updates, predictions and their rollback), and full chunk data through
 * {@code replaceWithPacketData}. Both hooks run at HEAD, so calls that turn out to change nothing still
 * count; that only costs a cache rebuild.
 */
@Mixin(LevelChunk.class)
public class LevelChunkMixin implements ChunkBlockVersion {

    @Unique
    private int blindspot$blockVersion;

    @Override
    public int blindspot$blockVersion() {
        return blindspot$blockVersion;
    }

    @Inject(method = "setBlockState", at = @At("HEAD"))
    private void blindspot$onSetBlockState(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir) {
        blindspot$blockVersion++;
    }

    @Inject(method = "replaceWithPacketData", at = @At("HEAD"))
    private void blindspot$onReplaceWithPacketData(FriendlyByteBuf buffer, Map<Heightmap.Types, long[]> heightmaps,
            Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> blockEntities, CallbackInfo ci) {
        blindspot$blockVersion++;
    }
}
