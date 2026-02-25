package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.github.kd_gaming1.scaleme.util.PerTickCache;
import com.github.kd_gaming1.scaleme.util.ScaleResolver;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {

    @Inject(
            method = "shouldShowName(Lnet/minecraft/world/entity/Avatar;D)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void scaleme$showOwnNametagInThirdPerson(Avatar entity, double distanceToCameraSq, CallbackInfoReturnable<Boolean> cir) {
        if (!ScaleMeConfig.showOwnNametagInThirdPerson) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Only your own nametag
        if (entity != mc.player) return;

        // Only in third person
        CameraType cam = mc.options.getCameraType();
        if (cam.isFirstPerson()) return;

        cir.setReturnValue(true);
    }

    @Inject(method = "scale*", at = @At("TAIL"))
    private void onScale(AvatarRenderState state, PoseStack poseStack, CallbackInfo ci) {
        if (ScaleResolver.noScalingConfigured()) return;

        float scale = PerTickCache.getScale(state.id);
        if (scale == 1f) return;

        poseStack.scale(scale, scale, scale);
    }

    @Inject(method = "extractRenderState*", at = @At("TAIL"))
    private void scaleme$adjustNameTagAttachment(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        if (ScaleResolver.noScalingConfigured()) return;
        if (!ScaleMeConfig.scaleNameTags) return;
        if (state.nameTagAttachment == null) return;

        float scale = PerTickCache.getScale(state.id);
        if (scale == 1f) return;

        double y = state.nameTagAttachment.y;
        state.nameTagAttachment = new Vec3(state.nameTagAttachment.x, y * scale, state.nameTagAttachment.z);
    }
}