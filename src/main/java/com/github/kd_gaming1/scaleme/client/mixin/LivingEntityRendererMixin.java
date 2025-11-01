package com.github.kd_gaming1.scaleme.client.mixin;

import com.github.kd_gaming1.scaleme.client.util.ScaleManager;
import com.github.kd_gaming1.scaleme.client.util.ScaleTransformer;
import com.github.kd_gaming1.scaleme.client.util.VillagerEntityRenderStateAccessor;
import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.VillagerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to handle scaling for living entities, specifically villager NPCs.
 * <p>
 * This focuses on Hypixel villager NPCs when the appropriate config is enabled.
 */
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    /**
     * Applies custom scaling to villager NPCs.
     * <p>
     * Injected at HEAD to intercept before vanilla scaling logic.
     * Cancels vanilla scaling when custom scaling is applied.
     */
    @Inject(
            method = "scale(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void applyVillagerNpcScaling(
            LivingEntityRenderState renderState,
            MatrixStack matrices,
            CallbackInfo ci) {

        if (renderState == null || matrices == null) {
            return;
        }

        // Only process villager render states
        if (!(renderState instanceof VillagerEntityRenderState villagerRenderState)) {
            return;
        }

        if (scaleme$tryApplyVillagerScaling(villagerRenderState, matrices)) {
            ci.cancel(); // Prevent vanilla scaling
        }
    }

    // ===== Helper Methods =====

    /**
     * Attempts to apply villager NPC scaling if enabled and applicable.
     *
     * @param villagerRenderState The villager render state
     * @param matrices The matrix stack for transformations
     * @return true if scaling was applied and vanilla should be cancelled
     */
    @Unique
    private boolean scaleme$tryApplyVillagerScaling(
            VillagerEntityRenderState villagerRenderState,
            MatrixStack matrices) {

        if (!ScaleMeConfig.enableVillagerNpcScaling) {
            return false;
        }

        VillagerEntityRenderStateAccessor accessor =
                (VillagerEntityRenderStateAccessor) villagerRenderState;
        VillagerEntity villager = accessor.scaleme$getVillagerEntity();

        if (villager == null) {
            return false;
        }

        // Apply villager NPC scale from scale manager
        float scale = ScaleManager.getVillagerNpcScale();
        ScaleTransformer.applyScale(matrices, scale);

        return true;
    }
}