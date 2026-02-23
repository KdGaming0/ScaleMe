package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.github.kd_gaming1.scaleme.util.HandContext;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hooks into {@link ItemInHandRenderer} for swing and hand-context overrides. */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    private static final float PI = (float) Math.PI;

    @Shadow private void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float inverseArmHeight) {}
    @Shadow private void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm arm, float attackProgress) {}

    // ── Swing Bobbing ───────────────────────────────────────────────────────

    /** Forces attack-strength scale to 1.0 so the item never dips on attack. */
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
    private float scaleme$suppressSwingBobbing(float originalScale) {
        if (!ScaleMeConfig.enableAnimOverrides || !ScaleMeConfig.disableSwingBobbing) return originalScale;
        return 1.0f;
    }

    // ── Hand Context ────────────────────────────────────────────────────────

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void scaleme$captureHand(
            AbstractClientPlayer player,
            float tickDelta, float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack heldItem,
            float equipProgress,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            CallbackInfo ci) {

        HandContext.renderDepth++;

        if (ScaleMeConfig.enableHandItemTransform) {
            HandContext.update(hand);
        } else {
            HandContext.currentHand = hand;
        }
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"))
    private void scaleme$releaseHand(
            AbstractClientPlayer player,
            float tickDelta, float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack heldItem,
            float equipProgress,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            CallbackInfo ci) {

        HandContext.renderDepth--;
        if (HandContext.renderDepth <= 0) {
            HandContext.renderDepth = 0;
            HandContext.currentHand = null;
        }
    }

    // ── Swing Drift Override ────────────────────────────────────────────────

    /** Replaces vanilla's fixed swing-drift translation with per-axis configurable amounts.
     *  Vanilla defaults: X = -0.4, Y = 0.2, Z = -0.2 */
    //? if >=1.21.11 {
    /*@Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    private void scaleme$overrideSwingDrift(float attackProgress, PoseStack poseStack, int handSide, HumanoidArm arm, CallbackInfo ci) {
    *///?} else {
    @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    private void scaleme$overrideSwingDrift(float attackProgress, float inverseArmHeight, PoseStack poseStack, int handSide, HumanoidArm arm, CallbackInfo ci) {
        //?}

        if (!ScaleMeConfig.enableAnimOverrides || !ScaleMeConfig.enableSwingOverride) return;
        ci.cancel();

        float sqrtAttack = Mth.sqrt(attackProgress);

        float driftX = ScaleMeConfig.swingArmXScale * Mth.sin(sqrtAttack * PI);
        float driftY = ScaleMeConfig.swingArmYScale * Mth.sin(sqrtAttack * (PI * 2));
        float driftZ = ScaleMeConfig.swingArmZScale * Mth.sin(attackProgress * PI);

        poseStack.translate(ScaleMeConfig.swingArmXMultiplyBySide ? handSide * driftX : driftX, driftY, driftZ);

        //? if =1.21.10 {
        applyItemArmTransform(poseStack, arm, inverseArmHeight);
        //?}
        applyItemArmAttackTransform(poseStack, arm, attackProgress);
    }

    // ── Swing Arc Override ──────────────────────────────────────────────────

    /** Replaces vanilla's fixed swing-arc rotations with per-axis configurable amounts.
     *  Vanilla defaults: preRotation Y = 45°, arc Y = -20°, Z = -20°, X chop = -80° */
    @Inject(method = "applyItemArmAttackTransform", at = @At("HEAD"), cancellable = true)
    private void scaleme$overrideSwingArc(PoseStack poseStack, HumanoidArm arm, float attackProgress, CallbackInfo ci) {
        if (!ScaleMeConfig.enableAnimOverrides || !ScaleMeConfig.enableSwingOverride) return;
        ci.cancel();

        int armSideSign = (arm == HumanoidArm.RIGHT) ? 1 : -1;

        float lateSwingCurve = Mth.sin(attackProgress * attackProgress * PI);  // peaks late, drives Y arc
        float midSwingCurve  = Mth.sin(Mth.sqrt(attackProgress) * PI);         // peaks mid, drives Z tilt + X chop

        poseStack.mulPose(Axis.YP.rotationDegrees(armSideSign * (ScaleMeConfig.swingPreRotationY + lateSwingCurve * ScaleMeConfig.swingArcYAmount)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(armSideSign * midSwingCurve * ScaleMeConfig.swingArcZAmount));
        poseStack.mulPose(Axis.XP.rotationDegrees(midSwingCurve * ScaleMeConfig.swingArcXAmount));

        // Undoes the Y pre-rotation so the item swings back to center (disable for 1.8-style swing)
        if (ScaleMeConfig.swingCounterRotation) {
            poseStack.mulPose(Axis.YP.rotationDegrees(armSideSign * -ScaleMeConfig.swingPreRotationY));
        }
    }
}