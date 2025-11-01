package com.github.kd_gaming1.scaleme.client.mixin;

import com.github.kd_gaming1.scaleme.client.util.ScaleConstants;
import com.github.kd_gaming1.scaleme.client.util.ScaleTransformer;
import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to handle custom scaling and positioning of held items in first person.
 * Applies transformations before the item is rendered.
 */
@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    /**
     * Applies custom scale, rotation, and position transformations to held items.
     * <p>
     * Injection point is right before the item rendering call, allowing us to
     * modify the matrix stack before the item is drawn.
     */
    @Inject(
            method = "renderFirstPersonItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
            )
    )
    private void applyItemTransformations(
            CallbackInfo ci,
            @Local(argsOnly = true) Hand hand,
            @Local(argsOnly = true) MatrixStack matrices,
            @Local ItemStack item) {

        // Early exit if feature disabled or no item
        if (!shouldApplyTransformations(item)) {
            return;
        }

        // Build and apply transformation configuration
        ScaleTransformer.ItemTransformConfig config =
                ScaleTransformer.ItemTransformConfig.fromConfig();

        if (config.hasTransformations()) {
            ScaleTransformer.applyItemTransform(matrices, config);
        }
    }

    /**
     * Modifies the attack cooldown progress to disable swing animation bobbing.
     * <p>
     * When bobbing is disabled, returns 1.0f (full cooldown) instead of the actual
     * progress, which prevents the visual bobbing effect.
     */
    @ModifyExpressionValue(
            method = "updateHeldItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttackCooldownProgress(F)F"
            )
    )
    private float modifySwingBobbing(float originalCooldown) {
        if (shouldDisableSwingBobbing()) {
            return ScaleConstants.SWING_BOBBING_DISABLED_VALUE;
        }
        return originalCooldown;
    }

    // ===== Helper Methods =====

    /**
     * Determines if item transformations should be applied.
     * @param item The item stack being rendered
     * @return true if transformations should apply
     */
    private boolean shouldApplyTransformations(ItemStack item) {
        return ScaleMeConfig.enableItemScaleAndPosition &&
                item != null &&
                !item.isEmpty();
    }

    /**
     * Determines if swing animation bobbing should be disabled.
     * @return true if bobbing should be disabled
     */
    private boolean shouldDisableSwingBobbing() {
        return (ScaleMeConfig.enableItemScaleAndPosition ||
                ScaleMeConfig.enableItemSwingModifications) &&
                ScaleMeConfig.disableSwingAnimationBobbing;
    }
}