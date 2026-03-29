package net.mastersplasher.savestate;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mastersplasher.savestate.Payload.PausePayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Savestate implements ModInitializer {
	public static final String MOD_ID = "savestate";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.serverboundPlay().register(PausePayload.ID, PausePayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(PausePayload.ID, ((payload, context) -> {
			context.server().execute(() -> {
				var tickManager = context.server().tickRateManager();
				tickManager.setFrozen(SavestateClient.isFrozen);
			});
		}));
	}
}