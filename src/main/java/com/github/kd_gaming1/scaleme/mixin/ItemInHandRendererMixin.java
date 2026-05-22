package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.github.kd_gaming1.scaleme.util.BlockingState;
import com.github.kd_gaming1.scaleme.util.FeatureFlags;
import com.github.kd_gaming1.scaleme.util.HandContext;
import com.github.kd_gaming1.scaleme.util.SwordBlockPose;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hooks into {@link ItemInHandRenderer} for swing and hand-context overrides. */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Unique
    private static final float PI = (float) Math.PI;

    @Shadow private void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float inverseArmHeight) {}
    @Shadow private void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm arm, float attackProgress) {}

    // ── Swing Bobbing ───────────────────────────────────────────────────────

    /**
     * Forces attack-strength scale to 1.0 so the item never dips on attack.
     * Uses {@link WrapOperation} for clean parameter access.
     * The target method was renamed between 1.21.10 and 1.21.11,
     * so the {@code @At} target is the only Stonecutter-branched line.
     */
    @WrapOperation(
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
    private float scaleme$suppressSwingBobbing(LocalPlayer player, float partialTick, Operation<Float> original) {
        if (!FeatureFlags.isEnabled(FeatureFlags.ANIM_OVERRIDES | FeatureFlags.DISABLE_SWING_BOB)) {
            return original.call(player, partialTick);
        }
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

        if (FeatureFlags.isEnabled(FeatureFlags.HAND_TRANSFORM)) {
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

    /**
     * Replaces vanilla's fixed swing-drift translation with per-axis configurable amounts.
     * The method signature differs between 1.21.10 (has {@code inverseArmHeight}) and
     * 1.21.11 (removed it). Only the signature line is version-branched to keep the
     * Stonecutter conditional as small as possible.
     *
     * <p>Vanilla defaults: X = -0.4, Y = 0.2, Z = -0.2
     */
    //? if >=1.21.11 {
    /*@Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    private void scaleme$overrideSwingDrift(float attackProgress, PoseStack poseStack, int handSide, HumanoidArm arm, CallbackInfo ci) {
    *///?} else {
    @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    private void scaleme$overrideSwingDrift(float attackProgress, float inverseArmHeight, PoseStack poseStack, int handSide, HumanoidArm arm, CallbackInfo ci) {
        //?}

        if (!FeatureFlags.isEnabled(FeatureFlags.ANIM_OVERRIDES | FeatureFlags.SWING_OVERRIDE)) return;
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
        if (!FeatureFlags.isEnabled(FeatureFlags.ANIM_OVERRIDES | FeatureFlags.SWING_OVERRIDE)) return;
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

    /** Replaces the vanilla arm anchor position and height-bob scale with configurable values.
     *  Vanilla: translate(invert * 0.56, -0.52 + inverseArmHeight * -0.6, -0.72) */
    @Inject(method = "applyItemArmTransform", at = @At("HEAD"), cancellable = true)
    private void scaleme$overrideArmBasePosition(PoseStack poseStack, HumanoidArm arm, float inverseArmHeight, CallbackInfo ci) {
        if (!FeatureFlags.isEnabled(FeatureFlags.HAND_TRANSFORM | FeatureFlags.ARM_POSITION)) return;
        ci.cancel();

        int invert = (arm == HumanoidArm.RIGHT) ? 1 : -1;

        boolean useOffhand = (HandContext.currentHand == InteractionHand.OFF_HAND)
                && FeatureFlags.isEnabled(FeatureFlags.SEPARATE_OFFHAND);

        float baseX = useOffhand ? ScaleMeConfig.armBaseXOffhand : ScaleMeConfig.armBaseX;
        float baseY = useOffhand ? ScaleMeConfig.armBaseYOffhand : ScaleMeConfig.armBaseY;
        float baseZ = useOffhand ? ScaleMeConfig.armBaseZOffhand : ScaleMeConfig.armBaseZ;
        float heightScale = useOffhand ? ScaleMeConfig.armHeightScaleOffhand : ScaleMeConfig.armHeightScale;

        poseStack.translate(invert * baseX, baseY + inverseArmHeight * heightScale, baseZ);
    }

    /** Applies a blocking pose when holding a sword and right-clicking. */
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void scaleme$applySwordBlockPose(
            AbstractClientPlayer player, float tickDelta, float pitch,
            InteractionHand hand, float swingProgress, ItemStack heldItem,
            float equipProgress, PoseStack poseStack,
            SubmitNodeCollector collector, int packedLight,
            CallbackInfo ci) {

        if (!BlockingState.isBlocking
                || hand != InteractionHand.MAIN_HAND
                || !heldItem.is(ItemTags.SWORDS)) return;

        ci.cancel();

        poseStack.pushPose();
        HumanoidArm arm = player.getMainArm();
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;

        applyItemArmTransform(poseStack, arm, equipProgress);
        poseStack.translate(side * SwordBlockPose.TRANSLATE_X, SwordBlockPose.TRANSLATE_Y, SwordBlockPose.TRANSLATE_Z);
        poseStack.mulPose(Axis.XP.rotationDegrees(SwordBlockPose.ROTATE_X));
        poseStack.mulPose(Axis.YP.rotationDegrees(side * SwordBlockPose.ROTATE_Y));
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * SwordBlockPose.ROTATE_Z));

        ((ItemInHandRenderer) (Object) this).renderItem(
                player,
                heldItem,
                arm == HumanoidArm.RIGHT
                        ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                        : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                poseStack,
                collector,
                packedLight
        );
        poseStack.popPose();

        // --- release HandContext --- //
        HandContext.renderDepth--;
        if (HandContext.renderDepth <= 0) {
            HandContext.renderDepth = 0;
            HandContext.currentHand = null;
        }
    }
}
