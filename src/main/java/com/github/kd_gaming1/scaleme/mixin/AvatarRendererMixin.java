package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
}