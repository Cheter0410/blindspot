package com.cheter0410.blindspot.client.mixin;

import com.cheter0410.blindspot.client.cache.BoatStatusCache;
import com.cheter0410.blindspot.client.cache.BoatStatusCache.BoatStatus;
import com.cheter0410.blindspot.client.cache.CacheVerification;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Caches the boat status (in water, under water, on land, in air). Vanilla recomputes it every tick for
 * every boat by scanning the fluids and block collision shapes around it, even when neither the boat
 * nor any block near it changed, e.g. for boats that are parked or moved by the server.
 * <p>
 * The status only depends on the bounding box and the block and fluid states around it, so the cache is
 * reused while the box is equal and none of the surrounding chunks changed (see {@link BoatStatusCache}).
 * Collision shapes of blocks marked as dynamic by vanilla (pistons, shulker boxes, ...) can change without
 * a block change, so results that depend on one are never cached. Only applies to the client world.
 */
@Mixin(AbstractBoat.class)
public class AbstractBoatMixin {

    @Shadow
    private double waterLevel;

    @Shadow
    private float landFriction;

    @Unique
    private static final CacheVerification blindspot$statusVerification =
            CacheVerification.ENABLED ? new CacheVerification("Boat status") : null;

    @Unique
    private BoatStatusCache blindspot$statusCache;

    @Unique
    private boolean blindspot$readDynamicShape;

    @WrapMethod(method = "getStatus")
    private AbstractBoat.Status blindspot$useCachedStatus(Operation<AbstractBoat.Status> original) {
        Entity self = (Entity) (Object) this;
        Level level = self.level();
        if (!level.isClientSide()) {
            return original.call();
        }

        AABB box = self.getBoundingBox();
        BoatStatusCache cache = blindspot$statusCache;
        if (cache != null && cache.isValidFor(level, box)) {
            BoatStatus cached = cache.result();
            if (CacheVerification.ENABLED) {
                blindspot$statusVerification.hit(blindspot$computeStatus(original), cached);
            }
            // Replay vanilla's side effects: waterLevel is written on every path, landFriction only on land.
            waterLevel = cached.waterLevel();
            if (cached.status() == AbstractBoat.Status.ON_LAND) {
                landFriction = cached.landFriction();
            }
            return cached.status();
        }

        if (CacheVerification.ENABLED) {
            blindspot$statusVerification.miss();
        }

        blindspot$readDynamicShape = false;
        BoatStatus result = blindspot$computeStatus(original);
        blindspot$statusCache = blindspot$readDynamicShape ? null : BoatStatusCache.create(level, box, result);
        return result.status();
    }

    @Unique
    private BoatStatus blindspot$computeStatus(Operation<AbstractBoat.Status> original) {
        AbstractBoat.Status status = original.call();
        return new BoatStatus(status, waterLevel, status == AbstractBoat.Status.ON_LAND ? landFriction : 0.0F);
    }

    // Shapes of non-dynamic blocks are cached per block state by vanilla itself, so only dynamic ones can vary in place.
    @WrapOperation(method = "getGroundFriction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
    private VoxelShape blindspot$noteDynamicShape(BlockState state, BlockGetter level, BlockPos pos, Operation<VoxelShape> original) {
        if (state.getBlock().hasDynamicShape()) {
            blindspot$readDynamicShape = true;
        }
        return original.call(state, level, pos);
    }
}
