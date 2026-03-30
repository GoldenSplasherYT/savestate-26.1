package net.mastersplasher.savestate;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mastersplasher.savestate.Payload.LoadPayload;
import net.mastersplasher.savestate.Payload.PausePayload;
import net.mastersplasher.savestate.Payload.SavePayload;
import net.minecraft.server.level.ServerPlayer;
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
		PayloadTypeRegistry.serverboundPlay().register(SavePayload.ID, SavePayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(LoadPayload.ID, LoadPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(PausePayload.ID, ((payload, context) -> {
			context.server().execute(() -> {
				var tickManager = context.server().tickRateManager();
				tickManager.setFrozen(SavestateClient.isFrozen);
			});
		}));

		ServerPlayNetworking.registerGlobalReceiver(SavePayload.ID, ((payload, context) -> {
			context.server().execute(() -> {
				ServerPlayer player = context.player();

				pos.set(new Vector3d(player.getX(), player.getY(), player.getZ()));
			});
		}));

		ServerPlayNetworking.registerGlobalReceiver(LoadPayload.ID, ((payload, context) -> {
			context.server().execute(() -> {
				ServerPlayer player = context.player();

				player.setPos(pos.get().x, pos.get().y, pos.get().z);
			});
		}));
	}
}