package com.github.kd_gaming1.scaleme.client.mixin;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @Inject(method = "renderFirstPersonItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void scaleHeldItem(CallbackInfo ci,
                               @Local(argsOnly = true) Hand hand,
                               @Local(argsOnly = true) MatrixStack matrices,
                               @Local ItemStack item) {

        // Check if item scaling is enabled
        if (!ScaleMeConfig.enableItemScaleAndPosition || item.isEmpty()) {
            return;
        }

        // Apply rotations first
        if (ScaleMeConfig.heldItemPitchRotation != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(ScaleMeConfig.heldItemPitchRotation));
        }
        if (ScaleMeConfig.heldItemYawRotation != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(ScaleMeConfig.heldItemYawRotation));
        }
        if (ScaleMeConfig.heldItemRollRotation != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(ScaleMeConfig.heldItemRollRotation));
        }

        // Apply scaling
        if (ScaleMeConfig.itemScale != 1.0f) {
            matrices.scale(ScaleMeConfig.itemScale, ScaleMeConfig.itemScale, ScaleMeConfig.itemScale);
        }

        // Apply position adjustments
        if (ScaleMeConfig.heldItemXPosition != 0.0f ||
                ScaleMeConfig.heldItemYPosition != 0.0f ||
                ScaleMeConfig.heldItemZPosition != 0.0f) {

            matrices.translate(
                    ScaleMeConfig.heldItemXPosition / ScaleMeConfig.itemScale,
                    ScaleMeConfig.heldItemYPosition / ScaleMeConfig.itemScale,
                    ScaleMeConfig.heldItemZPosition / ScaleMeConfig.itemScale
            );
        }
    }

    @ModifyExpressionValue(method = "updateHeldItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttackCooldownProgress(F)F"))
    private float preventSwingAnimationBobbing(float original) {
        if (ScaleMeConfig.enableItemScaleAndPosition || ScaleMeConfig.enableItemSwingModifications) {
            return ScaleMeConfig.disableSwingAnimationBobbing ? 1.0f : original;
        }
        return original;
    }
}