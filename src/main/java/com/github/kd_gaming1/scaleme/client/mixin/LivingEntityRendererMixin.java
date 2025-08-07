package com.github.kd_gaming1.scaleme.client.mixin;

import com.github.kd_gaming1.scaleme.client.util.HypixelNpcUtil;
import com.github.kd_gaming1.scaleme.client.util.ScaleManager;
import com.github.kd_gaming1.scaleme.client.util.VillagerEntityRenderStateAccessor;
import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.VillagerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(method = "scale(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void scaleVillagerNpc(LivingEntityRenderState renderState, MatrixStack matrixStack, CallbackInfo ci) {
        // Check if this is a villager render state
        if (renderState instanceof VillagerEntityRenderState villagerRenderState) {
            VillagerEntityRenderStateAccessor accessor = (VillagerEntityRenderStateAccessor) villagerRenderState;
            VillagerEntity villager = accessor.scaleme$getVillagerEntity();

            // Check if this is a Hypixel NPC and scaling is enabled
            if (ScaleMeConfig.enableVillagerNpcScaling) {
                float scale = ScaleManager.getVillagerNpcScale();
                if (scale != 1.0f) {
                    matrixStack.scale(scale, scale, scale);
                }
                ci.cancel(); // Prevent vanilla scaling
                return;
            }
        }
    }
}