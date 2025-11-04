package com.github.kd_gaming1.scaleme.client.mixin;

import com.github.kd_gaming1.scaleme.client.util.HypixelNpcUtil;
import com.github.kd_gaming1.scaleme.client.util.PlayerEntityRenderStateAccessor;
import com.github.kd_gaming1.scaleme.client.util.ScaleManager;
import com.github.kd_gaming1.scaleme.client.util.ScaleTransformer;
import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Mixin to handle player model scaling in the rendering pipeline.
 * <p>
 * This mixin operates in two phases:
 * <ol>
 *   <li>Store player data in render state during update</li>
 *   <li>Apply scaling transformations during rendering</li>
 * </ol>
 */
@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    /**
     * Stores player UUID and entity reference in the render state for later use.
     * <p>
     * Injected at TAIL of updateRenderState to ensure all vanilla data is populated first.
     */
    @Inject(
            //? if >=1.21.9 {
            /*method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
            at = @At("TAIL")
            *///?} else {
            method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
            at = @At("TAIL")
            //?}
    )
    private void storePlayerData(
            //? if >=1.21.9 {
            /*net.minecraft.entity.PlayerLikeEntity player,
            *///?} else {
            AbstractClientPlayerEntity player,
             //?}
            PlayerEntityRenderState renderState,
            float tickDelta,
            CallbackInfo ci) {

        if (player == null || renderState == null) {
            return;
        }

        //? if >=1.21.9 {
        /*// Cast PlayerLikeEntity to AbstractClientPlayerEntity for 1.21.9+
        if (!(player instanceof AbstractClientPlayerEntity clientPlayer)) {
            return;
        }
        PlayerEntityRenderStateAccessor accessor = (PlayerEntityRenderStateAccessor) renderState;
        accessor.scaleme$setPlayerUUID(clientPlayer.getUuid());
        accessor.scaleme$setPlayerEntity(clientPlayer);
        *///?} else {
        PlayerEntityRenderStateAccessor accessor = (PlayerEntityRenderStateAccessor) renderState;
        accessor.scaleme$setPlayerUUID(player.getUuid());
        accessor.scaleme$setPlayerEntity(player);
        //?}
    }


    /**
     * Applies scale transformations to player models based on configuration.
     * <p>
     * Handles three scaling scenarios:
     * <ul>
     *   <li>Hypixel NPCs (when enabled)</li>
     *   <li>Regular players (from presets or config)</li>
     *   <li>Default scaling (no transformation)</li>
     * </ul>
     * <p>
     * Cancels vanilla scaling when custom scaling is applied.
     */
    @Inject(
            method = "scale(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void applyCustomScaling(
            PlayerEntityRenderState renderState,
            MatrixStack matrices,
            CallbackInfo ci) {

        if (renderState == null || matrices == null) {
            return;
        }

        PlayerEntityRenderStateAccessor accessor = (PlayerEntityRenderStateAccessor) renderState;
        AbstractClientPlayerEntity player = accessor.scaleme$getPlayerEntity();
        UUID playerUUID = accessor.scaleme$getPlayerUUID();

        // Check if this is an NPC FIRST
        if (player != null && HypixelNpcUtil.isHypixelNpc(player)) {
            // This is an NPC - only apply NPC scaling, never player scaling
            if (ScaleMeConfig.enableNpcScaling) {
                float npcScale = ScaleManager.getNpcScale();
                ScaleTransformer.applyScale(matrices, npcScale);
                ci.cancel();
            }
            // Return regardless - NPCs should never use player scaling
            return;
        }

        // Not an NPC - apply regular player scaling
        if (scaleme$tryApplyPlayerScaling(playerUUID, matrices)) {
            ci.cancel();
        }
    }

    // ===== Helper Methods =====

    /**
     * Attempts to apply player-specific scaling from presets or config.
     *
     * @param playerUUID The player's UUID
     * @param matrices The matrix stack for transformations
     * @return true if player scaling was applied
     */
    @Unique
    private boolean scaleme$tryApplyPlayerScaling(UUID playerUUID, MatrixStack matrices) {
        if (playerUUID == null) {
            return false;
        }

        float scale = ScaleManager.getCurrentScale(playerUUID);
        ScaleTransformer.applyScale(matrices, scale);

        return true; // Always return true if we have a UUID (even if scale is 1.0)
    }
}