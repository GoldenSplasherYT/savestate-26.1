package net.mastersplasher.savestate;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class SavestateClient implements ClientModInitializer {

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

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (sendToChatKey.consumeClick()) {
                if (client.player != null) {
                    client.player.sendSystemMessage(Component.literal("Key was pressed!"));
                }
            }
        });

    }
}
