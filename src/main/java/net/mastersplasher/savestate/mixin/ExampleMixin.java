package net.mastersplasher.savestate.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class ExampleMixin {

    @ModifyArg(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"), index = 1)
    private float modifyTheDmg(float original) {
        return original * 3.0F;
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void attack(Entity entity, CallbackInfo ci) {
        ci.cancel();
    }


}