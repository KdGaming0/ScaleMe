package com.github.kd_gaming1.scaleme.client.mixin;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    @ModifyExpressionValue(
            method = "renderCrosshair(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/Perspective;isFirstPerson()Z")
    )
    private boolean shouldRenderCrosshair(boolean original) {
        if (original) {
            return true;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        Perspective perspective = client.options.getPerspective();

        if (perspective.isFrontView()) {
            return ScaleMeConfig.enableCrosshairInThirdPersonFront;
        } else {
            return ScaleMeConfig.enableCrosshairInThirdPerson;
        }
    }
}