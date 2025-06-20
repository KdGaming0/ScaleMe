package com.github.scaleme.client.util;

import java.util.UUID;

public interface PlayerEntityRenderStateAccessor {
    void scaleme$setPlayerUUID(UUID uuid);
    UUID scaleme$getPlayerUUID();
}