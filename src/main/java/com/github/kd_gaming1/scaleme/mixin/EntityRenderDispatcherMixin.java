package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.util.FeatureFlags;
import com.github.kd_gaming1.scaleme.util.HypixelLocationState;
import com.github.kd_gaming1.scaleme.util.NpcCache;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
//? if >=26.1 {
import net.minecraft.client.renderer.state.level.CameraRenderState;
//?} else {
/*import net.minecraft.client.renderer.state.CameraRenderState;
 *///?}
import net.minecraft.world.entity.EntityType;
//? if >=26.2 {
import net.minecraft.world.entity.EntityTypes;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(
            method = "submit",
            at = @At("HEAD"),
            cancellable = true
    )
    private <S extends EntityRenderState> void scaleme$hidePlayers(
            S renderState,
            CameraRenderState camera,
            double x,
            double y,
            double z,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CallbackInfo ci
    ) {
        if (!FeatureFlags.isEnabled(FeatureFlags.HIDE_PLAYERS)) return;
        if (FeatureFlags.isEnabled(FeatureFlags.HIDE_PLAYERS_SB_ONLY) && !HypixelLocationState.isOnSkyblock()) return;
        if (!(renderState instanceof AvatarRenderState avatarState)) return;
        //? if >=26.2 {
        if (avatarState.entityType != EntityTypes.PLAYER) return;
        //?} else {
        /*if (avatarState.entityType != EntityType.PLAYER) return;
        *///?}

        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player != null && avatarState.id == mc.player.getId()) return;

        // Don't hide Hypixel NPCs — only real players
        if (NpcCache.isHypixelNpc(avatarState.id)) return;

        ci.cancel();
    }
}