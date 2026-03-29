package net.mastersplasher.savestate.mixin;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.mastersplasher.savestate.SavestateClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMovementMixin {
    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void turnPlayer(double mousea, CallbackInfo ci) {
        if (SavestateClient.isFrozen) {
            ci.cancel();
        }
    }

    @Inject(method = "handleAccumulatedMovement", at = @At("HEAD"), cancellable = true)
    private void handleAccumulatedMovement(CallbackInfo ci) {
        if (SavestateClient.isFrozen) {
            ci.cancel();
        }
    }
}
