package com.github.scaleme.client.mixin;

import com.github.scaleme.config.ScaleMeConfig;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    /**
     * Redirects the perspective check in the crosshair rendering method to allow
     * crosshair visibility in third person based on configuration settings.
     */
    @Redirect(
            method = "renderCrosshair(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/Perspective;isFirstPerson()Z")
    )
    private boolean shouldRenderCrosshair(Perspective perspective) {
        // Always render in first person
        if (perspective.isFirstPerson()) {
            return true;
        }
        // Check configuration for third person views
        else if (perspective.isFrontView()) {
            return ScaleMeConfig.enableCrosshairInThirdPersonFront;
        } else {
            // Third person back view
            return ScaleMeConfig.enableCrosshairInThirdPerson;
        }
    }
}