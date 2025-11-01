package com.github.kd_gaming1.scaleme.client.mixin;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to modify camera perspective cycling behavior.
 * <p>
 * Allows skipping the front-facing (selfie) camera view when configured.
 */
@Mixin(Perspective.class)
public class PerspectiveMixin {

    /**
     * Intercepts perspective cycling to skip front view when selfie cam is disabled.
     * <p>
     * Normal cycle: FIRST_PERSON → THIRD_PERSON_BACK → THIRD_PERSON_FRONT → FIRST_PERSON
     * Modified cycle: FIRST_PERSON → THIRD_PERSON_BACK → FIRST_PERSON
     * <p>
     * This prevents players from accidentally entering front-facing view when they
     * have it disabled.
     *
     * @param cir Callback info returnable for the next perspective
     */
    @Inject(
            method = "next",
            at = @At("HEAD"),
            cancellable = true
    )
    private void skipFrontViewIfDisabled(CallbackInfoReturnable<Perspective> cir) {
        if (!ScaleMeConfig.disableSelfieCam) {
            return; // Allow normal cycling
        }

        Perspective current = (Perspective) (Object) this;

        // When in third-person back view and cycling forward,
        // skip front view and go directly to first person
        if (current == Perspective.THIRD_PERSON_BACK) {
            cir.setReturnValue(Perspective.FIRST_PERSON);
        }
    }
}