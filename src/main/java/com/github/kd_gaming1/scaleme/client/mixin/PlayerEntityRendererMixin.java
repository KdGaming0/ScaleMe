package com.github.kd_gaming1.scaleme.client.mixin;

import com.github.kd_gaming1.scaleme.client.util.PlayerEntityRenderStateAccessor;
import com.github.kd_gaming1.scaleme.client.util.ScaleManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
            at = @At("TAIL"))
    private void storePlayerUUID(AbstractClientPlayerEntity player, PlayerEntityRenderState renderState, float tickDelta, CallbackInfo ci) {
        // Store the player UUID in the render state for later use
        ((PlayerEntityRenderStateAccessor) renderState).scaleme$setPlayerUUID(player.getUuid());
    }

    @Inject(method = "scale(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;)V",
            at = @At("HEAD"))
    private void scalePlayerModel(PlayerEntityRenderState playerEntityRenderState, MatrixStack matrixStack, CallbackInfo ci) {
        java.util.UUID playerUUID = ((PlayerEntityRenderStateAccessor) playerEntityRenderState).scaleme$getPlayerUUID();

        if (playerUUID != null) {
            float scale = ScaleManager.getCurrentScale(playerUUID);
            if (scale != 1.0f) {
                matrixStack.scale(scale, scale, scale);
            }
        }
    }
}