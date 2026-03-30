package net.mastersplasher.savestate.mixin;

import net.mastersplasher.savestate.SavestateClient;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeltaTracker.Timer.class)
public class TimerMixin {
    @Inject(method = "updatePauseState", at = @At("HEAD"), cancellable = true)
    private void updatePauseState(boolean pauseState, CallbackInfo ci) {
        if (SavestateClient.isFrozen) {
            ci.cancel();
        }
    }
}
