package com.github.scaleme.client.mixin;

import com.github.scaleme.client.util.PlayerEntityRenderStateAccessor;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(PlayerEntityRenderState.class)
public class PlayerEntityRenderStateMixin implements PlayerEntityRenderStateAccessor {

    @Unique
    private UUID scaleme$playerUUID;

    @Override
    public void scaleme$setPlayerUUID(UUID uuid) {
        this.scaleme$playerUUID = uuid;
    }

    @Override
    public UUID scaleme$getPlayerUUID() {
        return this.scaleme$playerUUID;
    }
}