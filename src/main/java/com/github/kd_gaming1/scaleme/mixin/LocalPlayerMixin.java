package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.util.FeatureFlags;
import com.github.kd_gaming1.scaleme.util.SwingHoldState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Marks repeat swings started while the attack key remains held. */
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(method = "swing", at = @At("HEAD"))
    private void scaleme$trackHeldAttackSwing(InteractionHand hand, CallbackInfo ci) {
        boolean holdingAttack = FeatureFlags.isEnabled(FeatureFlags.SUPPRESS_REPEAT_SWING)
                && Minecraft.getInstance().options.keyAttack.isDown();
        SwingHoldState.onSwing(holdingAttack);
    }
}
