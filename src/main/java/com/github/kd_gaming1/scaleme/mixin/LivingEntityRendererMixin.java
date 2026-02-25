package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
//? if >=1.21.11 {
/*import net.minecraft.world.entity.npc.villager.Villager;
*///?} else {
import net.minecraft.world.entity.npc.Villager;
 //?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {

    @Inject(method = "extractRenderState*", at = @At("TAIL"))
    private void scaleme$villagerScale(T entity, S state, float partialTicks, CallbackInfo ci) {
        if (ScaleMeConfig.villagerNpcScale == 1f) return;
        if (!(entity instanceof Villager)) return;

        // Override the scale baked into render state
        state.scale = entity.getScale() * ScaleMeConfig.villagerNpcScale;
    }
}