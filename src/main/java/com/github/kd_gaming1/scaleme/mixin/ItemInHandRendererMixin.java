package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.github.kd_gaming1.scaleme.util.HandContext;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Mixins into {@link ItemInHandRenderer} to support animation and hand context overrides. */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    /**
     * Overrides the attack strength scale used during the swing animation tick.
     * When swing bobbing is disabled, returns 1.0 to suppress the effect.
     */
    @ModifyExpressionValue(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    //? if >=1.21.11 {
                    /*target = "Lnet/minecraft/client/player/LocalPlayer;getItemSwapScale(F)F"
                    *///?} else {
                    target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"
                    //?}
            )
    )
    private float scaleme$disableSwingBobbing_attackStrength(float original) {
        if (!ScaleMeConfig.enableAnimOverrides) return original;
        if (!ScaleMeConfig.disableSwingBobbing) return original;

        return 1.0F;
    }

    /** Populates {@link HandContext} with the current hand and its transform values before rendering. */
    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void captureHand(
            AbstractClientPlayer abstractClientPlayer,
            float f, float g,
            InteractionHand interactionHand,
            float h,
            ItemStack itemStack,
            float i,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int j,
            CallbackInfo ci) {
        HandContext.depth++;
        HandContext.currentHand = interactionHand;

        if (!ScaleMeConfig.enableHandItemTransform) return;

        boolean offhand = interactionHand == InteractionHand.OFF_HAND;
        boolean sep = offhand && ScaleMeConfig.enableSeparateHandTransforms;

        HandContext.tx = sep ? ScaleMeConfig.itemTranslationXOffhand : ScaleMeConfig.itemTranslationX;
        HandContext.ty = sep ? ScaleMeConfig.itemTranslationYOffhand : ScaleMeConfig.itemTranslationY;
        HandContext.tz = sep ? ScaleMeConfig.itemTranslationZOffhand : ScaleMeConfig.itemTranslationZ;

        HandContext.rx = sep ? ScaleMeConfig.itemRotationXOffhand : ScaleMeConfig.itemRotationX;
        HandContext.ry = sep ? ScaleMeConfig.itemRotationYOffhand : ScaleMeConfig.itemRotationY;
        HandContext.rz = sep ? ScaleMeConfig.itemRotationZOffhand : ScaleMeConfig.itemRotationZ;

        HandContext.s = sep ? ScaleMeConfig.itemScaleOffhand : ScaleMeConfig.itemScale;
    }

    /** Clears {@link HandContext} after rendering completes. */
    @Inject(method = "renderArmWithItem", at = @At("RETURN"))
    private void releaseHand(
            AbstractClientPlayer abstractClientPlayer,
            float f, float g,
            InteractionHand interactionHand,
            float h,
            ItemStack itemStack,
            float i,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int j,
            CallbackInfo ci) {
        HandContext.depth--;
        if (HandContext.depth <= 0) {
            HandContext.depth = 0;
            HandContext.currentHand = null;
        }
    }
}