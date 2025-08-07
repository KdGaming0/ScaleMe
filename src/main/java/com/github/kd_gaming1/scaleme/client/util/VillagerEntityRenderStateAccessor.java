package com.github.kd_gaming1.scaleme.client.util;

import net.minecraft.entity.passive.VillagerEntity;

public interface VillagerEntityRenderStateAccessor {
    void scaleme$setVillagerEntity(VillagerEntity villager);
    VillagerEntity scaleme$getVillagerEntity();
}