package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.renderer.item.ItemStackRenderState$LayerRenderState")
public class LayerRenderStateMixin {

    @Unique
    private boolean scaleme$didPush = false;


    @Inject(method = "submit", at = @At("HEAD"))
    private void onSubmitHead(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, int outlineColor, CallbackInfo ci) {
        if (!ScaleMeConfig.enableHandItemTransform) return;
        poseStack.pushPose();
        scaleme$didPush = true;
        PoseStack.Pose pose = poseStack.last();

        float tx = ScaleMeConfig.itemTranslationX;
        float ty = ScaleMeConfig.itemTranslationY;
        float tz = ScaleMeConfig.itemTranslationZ;
        if (tx != 0f || ty != 0f || tz != 0f) {
            pose.translate(tx, ty, tz);
        }

        float rx = ScaleMeConfig.itemRotationX;
        float ry = ScaleMeConfig.itemRotationY;
        float rz = ScaleMeConfig.itemRotationZ;
        if (rx != 0f || ry != 0f || rz != 0f) {
            pose.rotate(new Quaternionf().rotationXYZ(
                    rx * (float)(Math.PI / 180.0),
                    ry * (float)(Math.PI / 180.0),
                    rz * (float)(Math.PI / 180.0)
            ));
        }

        float s = ScaleMeConfig.itemScale;
        if (s != 1f) {
            pose.scale(s, s, s);
        }
    }

    @Inject(method = "submit", at = @At("RETURN"))
    private void onSubmitReturn(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, int outlineColor, CallbackInfo ci) {
        if (!ScaleMeConfig.enableHandItemTransform) return;
        if (scaleme$didPush) {
            poseStack.popPose();
            scaleme$didPush = false;
        }
    }
}
