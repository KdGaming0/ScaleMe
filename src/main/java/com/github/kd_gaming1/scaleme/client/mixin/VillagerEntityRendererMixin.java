package com.github.kd_gaming1.scaleme.client.mixin;

import com.github.kd_gaming1.scaleme.client.util.VillagerEntityRenderStateAccessor;
import net.minecraft.client.render.entity.VillagerEntityRenderer;
import net.minecraft.client.render.entity.state.VillagerEntityRenderState;
import net.minecraft.entity.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to store villager entity reference in render state for scaling.
 * <p>
 * This is required because the render state doesn't normally contain
 * the entity reference, but we need it to apply custom scaling.
 */
@Mixin(VillagerEntityRenderer.class)
public class VillagerEntityRendererMixin {

    /**
     * Stores the villager entity in the render state for later use during rendering.
     * <p>
     * Injected at TAIL to ensure all vanilla data is populated first.
     *
     * @param villager The villager entity being rendered
     * @param renderState The render state to populate
     * @param tickDelta Partial tick time
     * @param ci Callback info
     */
    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/passive/VillagerEntity;Lnet/minecraft/client/render/entity/state/VillagerEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void storeVillagerEntity(
            VillagerEntity villager,
            VillagerEntityRenderState renderState,
            float tickDelta,
            CallbackInfo ci) {

        if (villager == null || renderState == null) {
            return;
        }

        VillagerEntityRenderStateAccessor accessor = (VillagerEntityRenderStateAccessor) renderState;
        accessor.scaleme$setVillagerEntity(villager);
    }
}