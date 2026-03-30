package net.mastersplasher.savestate.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.fox.Fox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void onExtractRenderState(T entity, S state, float partialTicks, CallbackInfo ci) {
        if (entity instanceof Cow) {
            if (!Minecraft.getInstance().isPaused()) {
                //System.out.println("partialTicks: " + partialTicks + " Pos: " + entity.position() + " Old: " + entity.oldPosition());
            }
        }
    }
}
