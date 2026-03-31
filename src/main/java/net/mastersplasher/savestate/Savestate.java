package net.mastersplasher.savestate;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mastersplasher.savestate.meow.LoadPayload;
import net.mastersplasher.savestate.meow.PausePayload;
import net.mastersplasher.savestate.meow.SavePayload;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Savestate implements ModInitializer {
	public static final String MOD_ID = "savestate";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.serverboundPlay().register(PausePayload.ID, PausePayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SavePayload.ID, SavePayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(LoadPayload.ID, LoadPayload.CODEC);

		AtomicReference<Double> playerX = new AtomicReference<>(0.0);
		AtomicReference<Double> playerY = new AtomicReference<>(0.0);
		AtomicReference<Double> playerZ = new AtomicReference<>(0.0);


		ServerPlayNetworking.registerGlobalReceiver(PausePayload.ID, ((payload, context) -> context.server().execute(() -> {
            var server = context.server();
            var tickManager = server.tickRateManager();
            tickManager.setFrozen(payload.frozen());

            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            var origin = context.player();
            for (var player : players) {
                if (player.getUUID().equals(origin.getUUID())) continue;
                ServerPlayNetworking.send(player, new PausePayload(payload.frozen()));
            }
        })));

		ServerPlayNetworking.registerGlobalReceiver(SavePayload.ID, ((payload, context) -> context.server().execute(() -> {
            var server = context.server();
			ServerPlayer player = server.getPlayerList().getPlayers().getFirst();

			playerX.set(player.getX());
			playerY.set(player.getY());
			playerZ.set(player.getZ());

			System.out.println(playerX.get());
			System.out.println(playerY.get());
			System.out.println(playerZ.get());
        })));

		ServerPlayNetworking.registerGlobalReceiver(LoadPayload.ID, ((payload, context) -> context.server().execute(() -> {
			var server = context.server();
			ServerPlayer player = server.getPlayerList().getPlayers().getFirst();

			player.teleportTo(playerX.get(), playerY.get(), playerZ.get());

			System.out.println("Player is teleported to: x - " + playerX.get() + " y - " + playerY.get() + ", z - " + playerZ.get());
		})));
	}
}