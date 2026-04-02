package net.mastersplasher.savestate;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mastersplasher.savestate.payload.LoadPayload;
import net.mastersplasher.savestate.payload.PausePayload;
import net.mastersplasher.savestate.payload.SavePayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
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

		CompoundTag saveState = new CompoundTag();


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

			saveState.keySet().clear();

			saveState.putDouble("playerX", player.getX());
			saveState.putDouble("playerY", player.getY());
			saveState.putDouble("playerZ", player.getZ());
			saveState.putFloat("playerYaw", player.getYRot());
			saveState.putFloat("playerPitch", player.getXRot());
        })));

		ServerPlayNetworking.registerGlobalReceiver(LoadPayload.ID, ((payload, context) -> context.server().execute(() -> {
			var server = context.server();
			ServerPlayer player = server.getPlayerList().getPlayers().getFirst();

			player.teleportTo(saveState.getDoubleOr("x", player.getX()), saveState.getDoubleOr("y", player.getY()), saveState.getDoubleOr("z", player.getZ()));
		})));
	}
}