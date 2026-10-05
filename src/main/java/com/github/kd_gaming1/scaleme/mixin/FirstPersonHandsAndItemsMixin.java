package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.util.FeatureFlags;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 26.3 moved the held-item height ticking out of the renderer into this class. Targeted by name
 * so the mixin is simply never applied on versions where the class does not exist.
 */
@Mixin(targets = "net.minecraft.client.player.FirstPersonHandsAndItems")
public class FirstPersonHandsAndItemsMixin {

    /** Forces the item swap scale to 1.0 so the item never dips on attack. */
    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemSwapScale(F)F")
    )
    private float scaleme$suppressSwingBobbing(LocalPlayer player, float partialTick, Operation<Float> original) {
        if (!FeatureFlags.isEnabled(FeatureFlags.DISABLE_SWING_BOB)) {
            return original.call(player, partialTick);
        }
        return 1.0f;
    }
}
