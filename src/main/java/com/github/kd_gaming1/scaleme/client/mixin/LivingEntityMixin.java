package com.github.kd_gaming1.scaleme.client.mixin;

import com.github.kd_gaming1.scaleme.client.util.ScaleConstants;
import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Mixin to modify hand swing animations and durations for living entities.
 * <p>
 * Provides control over:
 * <ul>
 *   <li>Animation speed/duration</li>
 *   <li>Mining effect influence</li>
 *   <li>Haste/Mining Fatigue effects</li>
 * </ul>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    protected LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /**
     * Modifies mining effect checks (Haste/Mining Fatigue) when configured to ignore them.
     * <p>
     * This injection captures two boolean checks:
     * 1. StatusEffectUtil.hasHaste()
     * 2. hasStatusEffect() for Mining Fatigue
     * <p>
     * When {@code ignoreMiningEffects} is enabled, these checks are forced to false,
     * preventing mining effects from influencing animation speed.
     */
    @ModifyExpressionValue(
            method = "getHandSwingDuration",
            at = {
                    @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectUtil;hasHaste(Lnet/minecraft/entity/LivingEntity;)Z"),
                    @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z")
            },
            require = 2
    )
    private boolean modifyMiningEffectCheck(boolean originalHasEffect) {
        if (!scaleme$shouldModifySwing()) {
            return originalHasEffect;
        }

        // If we should ignore mining effects, return false regardless of actual effect status
        return !ScaleMeConfig.ignoreMiningEffects && originalHasEffect;
    }

    /**
     * Modifies the base swing duration constant to apply custom animation speed.
     */
    //? if >=1.21.11 {
    /*@ModifyVariable(
            method = "getHandSwingDuration",
            at = @At("STORE"),
            ordinal = 0
    )
    private int modifySwingDurationStoredBase(int vanillaDuration) {
        if (!scaleme$shouldModifySwing()) {
            return vanillaDuration;
        }

        if (ScaleMeConfig.disableItemAnimation) {
            return Integer.MAX_VALUE;
        }

        return scaleme$calculateModifiedDuration(vanillaDuration, ScaleMeConfig.itemAnimationSpeed);
    }
    *///?} else {
    @ModifyExpressionValue(
            method = "getHandSwingDuration",
            at = @At(value = "CONSTANT", args = "intValue=6")
    )
    private int modifySwingDuration(int vanillaDuration) {
        if (!scaleme$shouldModifySwing()) {
            return vanillaDuration;
        }

        if (ScaleMeConfig.disableItemAnimation) {
            return Integer.MAX_VALUE;
        }

        return scaleme$calculateModifiedDuration(vanillaDuration, ScaleMeConfig.itemAnimationSpeed);
    }
    //?}

    // ===== Helper Methods =====

    /**
     * Determines if swing modifications should be applied to this entity.
     * <p>
     * Modifications only apply to:
     * <ul>
     *   <li>The main client player</li>
     *   <li>When the feature is enabled</li>
     *   <li>When a client player exists</li>
     * </ul>
     *
     * @return true if modifications should apply
     */
    @Unique
    private boolean scaleme$shouldModifySwing() {
        if (!ScaleMeConfig.enableItemSwingModifications) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }

        // Only apply to the main player
        return (Object) this instanceof AbstractClientPlayerEntity player &&
                player.isMainPlayer();
    }

    /**
     * Calculates the modified swing duration based on animation speed.
     * <p>
     * Formula: modified = original / speed
     * Result is clamped to prevent extreme values.
     *
     * @param originalDuration The vanilla duration
     * @param animationSpeed The configured speed multiplier
     * @return The modified and clamped duration
     */
    @Unique
    private int scaleme$calculateModifiedDuration(int originalDuration, float animationSpeed) {
        // Prevent division by zero or negative speeds
        if (animationSpeed <= 0.0f) {
            return originalDuration;
        }

        int modified = Math.round(originalDuration / animationSpeed);

        // Clamp to safe range
        return Math.max(
                ScaleConstants.MIN_SWING_DURATION,
                Math.min(ScaleConstants.MAX_SWING_DURATION, modified)
        );
    }
}