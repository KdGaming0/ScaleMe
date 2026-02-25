package com.github.kd_gaming1.scaleme.mixin;

import com.github.kd_gaming1.scaleme.config.ScaleMeConfig;
import net.minecraft.client.CameraType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Skips THIRD_PERSON_FRONT when selfie cam is disabled.
 * <p>
 * Normal: FIRST_PERSON -> THIRD_PERSON_BACK -> THIRD_PERSON_FRONT -> FIRST_PERSON
 * New: FIRST_PERSON -> THIRD_PERSON_BACK -> FIRST_PERSON
 */
@Mixin(CameraType.class)
public class CameraTypeMixin {

    @Inject(method = "cycle", at = @At("HEAD"), cancellable = true)
    private void scaleme$skipFrontViewIfDisabled(CallbackInfoReturnable<CameraType> cir) {
        if (!ScaleMeConfig.disableSelfieCam) return;

        CameraType current = (CameraType) (Object) this;

        // If we are about to go BACK -> FRONT, go BACK -> FIRST instead
        if (current == CameraType.THIRD_PERSON_BACK) {
            cir.setReturnValue(CameraType.FIRST_PERSON);
        }
    }
}