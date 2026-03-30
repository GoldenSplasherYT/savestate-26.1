package net.mastersplasher.savestate;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mastersplasher.savestate.meow.PausePayload;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Savestate implements ModInitializer {
	public static final String MOD_ID = "savestate";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.serverboundPlay().register(PausePayload.ID, PausePayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(PausePayload.ID, ((payload, context) -> {
			context.server().execute(() -> {
				var server = context.server();
				var tickManager = server.tickRateManager();
				tickManager.setFrozen(payload.frozen());

				List<ServerPlayer> players = server.getPlayerList().getPlayers();
				var origin = context.player();
				for (var player : players) {
					if (player.getUUID().equals(origin.getUUID())) continue;
					ServerPlayNetworking.send(player, new PausePayload(payload.frozen()));
				}
			});
		}));
	}
}