package net.mastersplasher.savestate.mixin;

import net.mastersplasher.savestate.SavestateClient;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class PlayerModelMixin {

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void setupAnim(AvatarRenderState state, CallbackInfo ci) {
        if (SavestateClient.isFrozen) {
            ci.cancel();
        }
    }
}
