package com.github.kd_gaming1.scaleme.client.util;

import net.minecraft.client.network.AbstractClientPlayerEntity;

import java.util.UUID;

public interface PlayerEntityRenderStateAccessor {
    void scaleme$setPlayerUUID(UUID uuid);
    UUID scaleme$getPlayerUUID();

    void scaleme$setPlayerEntity(AbstractClientPlayerEntity player);
    AbstractClientPlayerEntity scaleme$getPlayerEntity();
}