package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.github.kd_gaming1.scaleme.util.FeatureFlags;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Mixins into {@link LivingEntity} to support swing animation overrides. */
@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    /** Vanilla default swing duration in ticks. */
    @Unique
    private static final int DEFAULT_SWING_DURATION = 6;

    /**
     * Modifies the swing duration based on config speed multiplier,
     * optionally ignoring potion/enchantment effects.
     */
    @Inject(method = "getCurrentSwingDuration", at = @At("RETURN"), cancellable = true)
    private void modifySwingDuration(CallbackInfoReturnable<Integer> cir) {
        if (!FeatureFlags.isEnabled(FeatureFlags.ANIM_OVERRIDES)) return;

        boolean ignore = FeatureFlags.isEnabled(FeatureFlags.IGNORE_SWING_SPEED);
        float speed = ScaleMeConfig.swingAnimationSpeed;

        if (!ignore && speed == 1f) return;

        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level().isClientSide()) return;

        var mc = Minecraft.getInstance();
        if (mc.player == null || self != mc.player) return;

        int duration = ignore ? DEFAULT_SWING_DURATION : cir.getReturnValue();
        if (speed != 1f) duration = Math.max(1, Math.round(duration / speed));

        cir.setReturnValue(duration);
    }

    /** Suppresses the attack animation for the local player when configured. */
    @Inject(method = "getAttackAnim", at = @At("RETURN"), cancellable = true)
    private void suppressAttackAnim(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (!FeatureFlags.isEnabled(FeatureFlags.ANIM_OVERRIDES | FeatureFlags.DISABLE_SWING_ANIM)) return;

        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level().isClientSide()) return;

        var mc = Minecraft.getInstance();
        if (self == mc.player) cir.setReturnValue(0f);
    }
}