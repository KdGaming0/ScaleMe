package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.util.FeatureFlags;
import com.github.kd_gaming1.scaleme.util.HandContext;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies the per-hand item transform from {@link HandContext} around each LayerRenderState submit call. */
@Mixin(targets = "net.minecraft.client.renderer.item.ItemStackRenderState$LayerRenderState")
public class LayerRenderStateMixin {

    @Unique private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    @Unique private boolean pushedPoseThisSubmit = false;

    // Reused every frame to avoid allocation; rotationXYZ overwrites contents each call so sharing is safe
    @Unique private final Quaternionf rotationQuaternion = new Quaternionf();

    @Inject(method = "submit", at = @At("HEAD"))
    private void onSubmitHead(PoseStack poseStack, SubmitNodeCollector collector,
                              int lightCoords, int overlayCoords, int outlineColor, CallbackInfo ci) {

        if (!FeatureFlags.isEnabled(FeatureFlags.ITEM_TRANSFORM)
                || HandContext.renderDepth == 0) return;
        if (!HandContext.activeTransform) return;

        poseStack.pushPose();
        pushedPoseThisSubmit = true;

        // Work directly on the top Pose to avoid redundant matrix multiplications
        PoseStack.Pose topPose = poseStack.last();

        if (HandContext.translationX != 0f || HandContext.translationY != 0f || HandContext.translationZ != 0f) {
            topPose.translate(HandContext.translationX, HandContext.translationY, HandContext.translationZ);
        }

        if (HandContext.rotationX != 0f || HandContext.rotationY != 0f || HandContext.rotationZ != 0f) {
            topPose.rotate(rotationQuaternion.rotationXYZ(
                    HandContext.rotationX * DEG_TO_RAD,
                    HandContext.rotationY * DEG_TO_RAD,
                    HandContext.rotationZ * DEG_TO_RAD));
        }

        if (HandContext.scale != 1f) {
            topPose.scale(HandContext.scale, HandContext.scale, HandContext.scale);
        }
    }

    @Inject(method = "submit", at = @At("RETURN"))
    private void onSubmitReturn(PoseStack poseStack, SubmitNodeCollector collector,
                                int lightCoords, int overlayCoords, int outlineColor, CallbackInfo ci) {
        if (pushedPoseThisSubmit) {
            poseStack.popPose();
            pushedPoseThisSubmit = false;
        }
    }
}