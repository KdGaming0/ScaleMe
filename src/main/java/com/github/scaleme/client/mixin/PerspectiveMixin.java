package com.github.scaleme.client.mixin;

import com.github.scaleme.config.ScaleMeConfig;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Perspective.class)
public class PerspectiveMixin {

    /**
     * Intercepts the perspective cycling to skip front view when selfie cam is disabled
     */
    @Inject(method = "next", at = @At("HEAD"), cancellable = true)
    private void skipFrontView(CallbackInfoReturnable<Perspective> cir) {
        if (ScaleMeConfig.disableSelfieCam) {
            Perspective current = (Perspective) (Object) this;

            // If we're currently in third person back view and trying to cycle to front view,
            // skip directly to first person instead
            if (current == Perspective.THIRD_PERSON_BACK) {
                cir.setReturnValue(Perspective.FIRST_PERSON);
            }
        }
    }
}