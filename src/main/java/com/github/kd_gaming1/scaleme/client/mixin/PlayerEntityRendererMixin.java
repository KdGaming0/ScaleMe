package com.github.kd_gaming1.scaleme.client.mixin;

import com.github.kd_gaming1.scaleme.client.util.PlayerEntityRenderStateAccessor;
import com.github.kd_gaming1.scaleme.client.util.ScaleManager;
import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.github.kd_gaming1.scaleme.client.util.HypixelNpcUtil; // import your helper
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
        PlayerEntityRenderStateAccessor accessor = (PlayerEntityRenderStateAccessor) renderState;
        accessor.scaleme$setPlayerUUID(player.getUuid());
        accessor.scaleme$setPlayerEntity(player);
    }

    @Inject(method = "scale(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void scalePlayerModel(PlayerEntityRenderState playerEntityRenderState, MatrixStack matrixStack, CallbackInfo ci) {
        PlayerEntityRenderStateAccessor accessor = (PlayerEntityRenderStateAccessor) playerEntityRenderState;
        java.util.UUID playerUUID = accessor.scaleme$getPlayerUUID();
        AbstractClientPlayerEntity player = accessor.scaleme$getPlayerEntity();

        // --- Regular Hypixel NPC Scaling ---
        if (ScaleMeConfig.enableNpcScaling && HypixelNpcUtil.isHypixelNpc(player)) {
            float scale = ScaleMeConfig.npcPlayerScale;
            if (scale != 1.0f) {
                matrixStack.scale(scale, scale, scale);
            }
            ci.cancel();
            return;
        }

        // --- Normal Player Scaling ---
        if (playerUUID != null) {
            float scale = ScaleManager.getCurrentScale(playerUUID);
            if (scale != 1.0f) {
                matrixStack.scale(scale, scale, scale);
            }
        }
    }
}