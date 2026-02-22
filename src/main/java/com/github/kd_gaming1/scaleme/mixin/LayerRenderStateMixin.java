package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.github.kd_gaming1.scaleme.util.HandContext;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Mixins into {@code LayerRenderState} to apply per-hand item transform overrides. */
@Mixin(targets = "net.minecraft.client.renderer.item.ItemStackRenderState$LayerRenderState")
public class LayerRenderStateMixin {

    @Unique private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    @Unique private boolean scaleme$didPush;

    @Unique private final Quaternionf scaleme$tmpQuat = new Quaternionf();

    /** Pushes a transformed pose before the item layer is submitted, if any transform is active. */
    @Inject(method = "submit", at = @At("HEAD"))
    private void onSubmitHead(PoseStack poseStack, SubmitNodeCollector collector,
                              int lightCoords, int overlayCoords, int outlineColor, CallbackInfo ci) {
        if (!ScaleMeConfig.enableHandItemTransform) return;
        if (HandContext.depth == 0) return;

        float tx = HandContext.tx, ty = HandContext.ty, tz = HandContext.tz;
        float rx = HandContext.rx, ry = HandContext.ry, rz = HandContext.rz;
        float s  = HandContext.s;

        if (tx == 0f && ty == 0f && tz == 0f &&
                rx == 0f && ry == 0f && rz == 0f && s == 1f) return;

        poseStack.pushPose();
        scaleme$didPush = true;

        PoseStack.Pose pose = poseStack.last();

        if (tx != 0f || ty != 0f || tz != 0f)
            pose.translate(tx, ty, tz);

        if (rx != 0f || ry != 0f || rz != 0f)
            pose.rotate(scaleme$tmpQuat.rotationXYZ(rx * DEG_TO_RAD, ry * DEG_TO_RAD, rz * DEG_TO_RAD));

        if (s != 1f)
            pose.scale(s, s, s);
    }

    /** Pops the pose pushed in {@link #onSubmitHead} if one was pushed. */
    @Inject(method = "submit", at = @At("RETURN"))
    private void onSubmitReturn(PoseStack poseStack, SubmitNodeCollector collector,
                                int lightCoords, int overlayCoords, int outlineColor, CallbackInfo ci) {
        if (!scaleme$didPush) return;
        poseStack.popPose();
        scaleme$didPush = false;
    }
}