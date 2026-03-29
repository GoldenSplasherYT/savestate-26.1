package net.mastersplasher.savestate.mixin;

import net.mastersplasher.savestate.SavestateClient;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends HumanoidRenderState> {

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void setupAnim(T state, CallbackInfo ci) {
        if (SavestateClient.isFrozen) {
            ci.cancel();
        }
    }
}
