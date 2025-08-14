package com.github.kd_gaming1.scaleme.client.mixin;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    protected LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyExpressionValue(method = "getHandSwingDuration",
            at = {
                    @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectUtil;hasHaste(Lnet/minecraft/entity/LivingEntity;)Z"),
                    @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z")
            },
            require = 2
    )
    private boolean ignoreMiningEffects(boolean original) {
        return (!shouldEnableSwingModifications() || !ScaleMeConfig.ignoreMiningEffects) && original;
    }

    @ModifyExpressionValue(method = "getHandSwingDuration", at = @At(value = "CONSTANT", args = "intValue=6"))
    private int modifySwingDuration(int original) {
        if (shouldEnableSwingModifications()) {
            int modified = Math.round(original / ScaleMeConfig.itemAnimationSpeed);
            return Math.max(1, Math.min(60, modified));
        }
        return original;
    }

    @Unique
    private boolean shouldEnableSwingModifications() {
        if (!ScaleMeConfig.enableItemSwingModifications) return false;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;

        if ((Object) this instanceof AbstractClientPlayerEntity p) {
            return p.isMainPlayer();
        }
        return false;
    }
}