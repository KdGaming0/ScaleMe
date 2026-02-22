package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Unique
    private static final int DEFAULT_SWING_DURATION = 6;

    @Inject(method = "getCurrentSwingDuration", at = @At("RETURN"), cancellable = true)
    private void modifySwingDuration(CallbackInfoReturnable<Integer> cir) {
        if (!ScaleMeConfig.enableAnimOverrides) return; // master toggle
        if (!ScaleMeConfig.ignoreSwingSpeedEffects && ScaleMeConfig.swingAnimationSpeed == 1f) return;

        LivingEntity self = (LivingEntity)(Object)this;
        if (!self.level().isClientSide() || self != Minecraft.getInstance().player) return;

        int duration = cir.getReturnValue();

        if (ScaleMeConfig.ignoreSwingSpeedEffects) {
            duration = DEFAULT_SWING_DURATION;
        }

        if (ScaleMeConfig.swingAnimationSpeed != 1f) {
            duration = Math.max(1, Math.round(duration / ScaleMeConfig.swingAnimationSpeed));
        }

        cir.setReturnValue(duration);
    }

    @Inject(method = "getAttackAnim", at = @At("RETURN"), cancellable = true)
    private void suppressAttackAnim(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (!ScaleMeConfig.enableAnimOverrides) return; // master toggle
        if (!ScaleMeConfig.disableSwingAnimation) return;

        LivingEntity self = (LivingEntity)(Object)this;
        if (self.level().isClientSide() && self == Minecraft.getInstance().player) {
            cir.setReturnValue(0f);
        }
    }
}