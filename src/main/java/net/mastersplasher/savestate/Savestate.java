package net.mastersplasher.savestate;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mastersplasher.savestate.Payload.PausePayload;
import org.joml.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class Savestate implements ModInitializer {
	public static final String MOD_ID = "savestate";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		AtomicReference<Vector3d> pos = null;

		PayloadTypeRegistry.serverboundPlay().register(PausePayload.ID, PausePayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(PausePayload.ID, ((payload, context) -> {
			context.server().execute(() -> {
				var tickManager = context.server().tickRateManager();
				tickManager.setFrozen(SavestateClient.isFrozen);
			});
		}));
	}
}