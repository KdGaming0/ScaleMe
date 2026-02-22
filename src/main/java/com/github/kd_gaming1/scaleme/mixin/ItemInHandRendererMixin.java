package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

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
    private float scaleme$disableSwingBobbing_attackStrength(float original) {
        if (!ScaleMeConfig.enableAnimOverrides) return original;
        if (!ScaleMeConfig.disableSwingBobbing) return original;

        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) return original;

        return 1.0F;
    }
}