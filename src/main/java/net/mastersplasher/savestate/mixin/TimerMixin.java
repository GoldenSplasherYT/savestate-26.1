package net.mastersplasher.savestate.mixin;

import net.mastersplasher.savestate.SavestateClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(DeltaTracker.Timer.class)
public class TimerMixin {
    @Inject(method = "updatePauseState", at = @At("HEAD"), cancellable = true)
    private void updatePauseState(boolean pauseState, CallbackInfo ci) {
        if (SavestateClient.isFrozen) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "getGameTimeDeltaPartialTick", at = @At("HEAD"), argsOnly = true, name = "ignoreFrozenGame")
    private boolean modifyGetGameTimeDeltaPartialTickArg(boolean ignoreFrozenGame) {
        if (SavestateClient.isFrozen || SavestateClient.isDeltaTickFrozen) {
            return true;
        }
        return ignoreFrozenGame;
    }
    @Inject(method = "updateFrozenState", at = @At("HEAD"))
    private void updateFrozenState(boolean frozenState, CallbackInfo ci) {
        if (!frozenState && !SavestateClient.isFrozen) {
            SavestateClient.isDeltaTickFrozen = false;
        }
    }

    @Inject(method = "advanceGameTime", at = @At("RETURN"))
    private void onAdvanceGameTime(long currentMs, CallbackInfoReturnable<Integer> cir) {
    }
}