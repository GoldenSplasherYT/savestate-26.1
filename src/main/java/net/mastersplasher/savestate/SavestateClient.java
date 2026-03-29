package net.mastersplasher.savestate;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mastersplasher.savestate.Payload.LoadPayload;
import net.mastersplasher.savestate.Payload.PausePayload;
import net.mastersplasher.savestate.Payload.SavePayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import org.lwjgl.glfw.GLFW;

public class SavestateClient implements ClientModInitializer {

    public static boolean isFrozen = false;

    KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(Savestate.MOD_ID, "savestate_keybinds")
    );

    KeyMapping sendToChatKey = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.savestate.send_to_chat",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_J,
                    CATEGORY
            ));

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
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (sendToChatKey.consumeClick()) {
                if (client.player != null) {
                    client.player.sendSystemMessage(Component.literal("Key was pressed!"));
                }
            }

            while (toggleFreezeKey.consumeClick()) {
                if (client.player != null) {
                    if (!isFrozen) {
                        isFrozen = true;
                        client.player.sendSystemMessage(Component.literal("Game is Frozen!"));
                    } else {
                        isFrozen = false;
                        client.player.sendSystemMessage(Component.literal("Game has now resumed!"));
                    }
                    ClientPlayNetworking.send(new PausePayload());
                }
            }

            while (savestateKey.consumeClick()) {
                if (client.player != null) {
                    if (isFrozen) {
                        client.player.sendSystemMessage(Component.literal("Savestate saved"));
                    } else {
                        client.player.sendSystemMessage(Component.literal("Savestate CANNOT be saved, as Game is not Frozen"));
                    }
                }
                ClientPlayNetworking.send(new SavePayload());
            }

            while (loadstateKey.consumeClick()) {
                if (client.player != null) {
                    client.player.sendSystemMessage(Component.literal("Savestate loaded"));
                }
                ClientPlayNetworking.send(new LoadPayload());
            }
        });

    }
}
