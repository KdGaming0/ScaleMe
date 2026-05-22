package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.util.FeatureFlags;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public class GuiMixin {

    @ModifyExpressionValue(
            method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z")
    )
    private boolean scaleme$shouldRenderCrosshair(boolean original) {
        if (original) return true;

        Minecraft mc = Minecraft.getInstance();
        CameraType cameraType = mc.options.getCameraType();

        if (cameraType == CameraType.THIRD_PERSON_FRONT) {
            return FeatureFlags.isEnabled(FeatureFlags.CROSSHAIR_3RD_FRONT);
        }
        return FeatureFlags.isEnabled(FeatureFlags.CROSSHAIR_3RD);
    }
}