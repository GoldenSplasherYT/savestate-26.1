package net.mastersplasher.savestate.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LocalPlayer.class)
public class PlayerMixin {

   // @ModifyArg(method = "tick", at = @At(""))
}
