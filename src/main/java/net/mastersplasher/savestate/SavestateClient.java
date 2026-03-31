package net.mastersplasher.savestate;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.mastersplasher.savestate.meow.LoadPayload;
import net.mastersplasher.savestate.meow.PausePayload;
import net.mastersplasher.savestate.meow.SavePayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class SavestateClient implements ClientModInitializer {
    public static boolean isFrozen;
    public static boolean isDeltaTickFrozen;

    KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(Savestate.MOD_ID, "savestate_keybinds")
    );

    KeyMapping toggleFreezeKey = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.savestate.toggle_freeze",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_F6,
                    CATEGORY
            ));

    KeyMapping savestateKey = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.savestate.savestate",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_F7,
                    CATEGORY
            ));

    KeyMapping loadstateKey = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.savestate.loadstate",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_F8,
                    CATEGORY
            ));

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.clientboundPlay().register(PausePayload.ID, PausePayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(PausePayload.ID, (payload, _) -> Minecraft.getInstance().execute(() -> {
            boolean frozen = payload.frozen();
            setFrozen(frozen);
        }));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleFreezeKey.consumeClick()) {
                if (client.player != null) {
                    boolean desired = !isFrozen;
                    setFrozen(desired);
                    ClientPlayNetworking.send(new PausePayload(desired));
                }
            }

            while (savestateKey.consumeClick()) {
                if (client.player != null) {
                    if (isFrozen) {
                        client.player.sendSystemMessage(Component.literal("Savestate saved"));
                        ClientPlayNetworking.send(new SavePayload());
                    } else {
                        client.player.sendSystemMessage(Component.literal("Savestate CANNOT be saved, as Game is not Frozen"));
                    }
                }
            }

            while (loadstateKey.consumeClick()) {
                if (client.player != null) {
                    client.player.sendSystemMessage(Component.literal("Savestate loaded"));
                }
                ClientPlayNetworking.send(new LoadPayload());

                if (!isFrozen) {
                    setFrozen(true);
                    ClientPlayNetworking.send(new PausePayload(true));
                }
            }
        });
    }

    private void setFrozen(boolean frozen) {
        Minecraft mc = Minecraft.getInstance();
        if (frozen) {
            ((DeltaTracker.Timer)mc.getDeltaTracker()).updatePauseState(true);
            isFrozen = true;
            isDeltaTickFrozen = true;
            if (Minecraft.getInstance().level != null) {
                Minecraft.getInstance().level.tickRateManager().setFrozen(true);
            }
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.literal("Game is Frozen!"));
            }
        } else {
            isFrozen = false;
            if (Minecraft.getInstance().level != null) {
                Minecraft.getInstance().level.tickRateManager().setFrozen(false);
            }
            ((DeltaTracker.Timer)mc.getDeltaTracker()).updatePauseState(false);
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.literal("Game has now resumed!"));
            }
        }
    }
}
