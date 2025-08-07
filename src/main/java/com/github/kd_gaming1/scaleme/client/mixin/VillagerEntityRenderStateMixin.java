package com.github.kd_gaming1.scaleme.client.mixin;

import com.github.kd_gaming1.scaleme.client.util.VillagerEntityRenderStateAccessor;
import net.minecraft.client.render.entity.state.VillagerEntityRenderState;
import net.minecraft.entity.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(VillagerEntityRenderState.class)
public class VillagerEntityRenderStateMixin implements VillagerEntityRenderStateAccessor {

    @Unique
    private VillagerEntity scaleme$villagerEntity;

    @Override
    public void scaleme$setVillagerEntity(VillagerEntity villager) {
        this.scaleme$villagerEntity = villager;
    }

    @Override
    public VillagerEntity scaleme$getVillagerEntity() {
        return this.scaleme$villagerEntity;
    }
}