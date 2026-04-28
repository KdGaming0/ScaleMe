package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import com.github.kd_gaming1.scaleme.util.HypixelLocationState;
import com.github.kd_gaming1.scaleme.util.HypixelNpcUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.EntityType;
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
        if (!ScaleMeConfig.hidePlayers) return;
        if (ScaleMeConfig.hidePlayersOnlyOnSkyblock && !HypixelLocationState.isOnSkyblock()) return;
        if (!(renderState instanceof AvatarRenderState avatarState)) return;
        if (avatarState.entityType != EntityType.PLAYER) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && avatarState.id == mc.player.getId()) return;

        // Don't hide Hypixel NPCs — only real players
        if (mc.level != null) {
            var entity = mc.level.getEntity(avatarState.id);
            if (entity instanceof AbstractClientPlayer player && HypixelNpcUtil.isHypixelNpc(player)) {
                return;
            }
        }

        ci.cancel();
    }
}
