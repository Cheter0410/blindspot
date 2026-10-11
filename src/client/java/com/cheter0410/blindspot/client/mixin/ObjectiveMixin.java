package com.cheter0410.blindspot.client.mixin;

import com.cheter0410.blindspot.client.cache.ObjectiveCacheHolder;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Holds the per-objective score version. It is increased by {@link ScoreboardMixin}, which sees every
 * score change.
 */
@Mixin(Objective.class)
public class ObjectiveMixin implements ObjectiveCacheHolder {

    @Unique
    private int blindspot$scoresVersion;

    @Override
    public int blindspot$scoresVersion() {
        return blindspot$scoresVersion;
    }

    @Override
    public void blindspot$incrementScoresVersion() {
        blindspot$scoresVersion++;
    }
}
