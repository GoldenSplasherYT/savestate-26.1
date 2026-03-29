package net.mastersplasher.savestate.mixin;

import net.minecraft.world.level.dimension.end.EnderDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonFight.class)
public class EndGatewayMixin {

    @Inject(method = "spawnExitPortal", at = @At("HEAD"), cancellable = true)
    public void place(boolean activated, CallbackInfo ci) {
        if (activated) {
            ci.cancel();
        }
    }
}
