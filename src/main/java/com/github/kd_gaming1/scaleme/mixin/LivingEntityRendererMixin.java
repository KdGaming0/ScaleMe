package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EntityType;
//? if >=26.2 {
import net.minecraft.world.entity.EntityTypes;
//?}
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {

    @Inject(method = "extractRenderState*", at = @At("TAIL"))
    private void scaleme$villagerScale(T entity, S state, float partialTicks, CallbackInfo ci) {
        if (ScaleMeConfig.villagerNpcScale == 1f) return;
        //? if >=26.2 {
        if (entity.getType() != EntityTypes.VILLAGER) return;
        //?} else {
        /*if (entity.getType() != EntityType.VILLAGER) return;
        *///?}

        state.scale = entity.getScale() * ScaleMeConfig.villagerNpcScale;
    }
}
