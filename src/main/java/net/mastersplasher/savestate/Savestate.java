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
import java.util.Set;
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
		ListTag playerDataListTags = new ListTag();

		// Freezes the Game
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

			saveState.putLong("SavedDayTime", player.level().getGameTime());

			for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
				CompoundTag playerTag = new CompoundTag();

				// Saving Player Data
				playerTag.putString("playerUUID", String.valueOf(serverPlayer.getUUID()));

				playerTag.putDouble("playerX", serverPlayer.getX());
				playerTag.putDouble("playerY", serverPlayer.getY());
				playerTag.putDouble("playerZ", serverPlayer.getZ());
				playerTag.putFloat("playerYaw", serverPlayer.getYRot());
				playerTag.putFloat("playerPitch", serverPlayer.getXRot());

				TagValueOutput playerOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, serverPlayer.level().registryAccess());
				serverPlayer.saveWithoutId(playerOutput);
				playerTag.put("FullPlayerData", playerOutput.buildResult());

				playerDataListTags.add(playerTag);
			}

			int radius = 64;
			BlockPos center = player.blockPosition();
			BlockPos min = new BlockPos(center.getX() - radius, center.getY() - radius, center.getZ() - radius);
			BlockPos max = new BlockPos(center.getX() + radius, center.getY() + radius, center.getZ() + radius);

			saveState.putInt("anchorX", min.getX());
			saveState.putInt("anchorY", min.getY());
			saveState.putInt("anchorZ", min.getZ());
			saveState.putInt("radius", radius);

			int size = (radius * 2 + 1);
			int[] blockData = new int[size * size * size];
			int index = 0;

			for (int y = min.getY(); y <= max.getY(); y++) {
				for (int z = min.getZ(); z <= max.getZ(); z++) {
					for (int x = min.getX(); x <= max.getX(); x++) {
						BlockState state = player.level().getBlockState(new BlockPos(x, y, z));
						blockData[index++] = Block.getId(state);
					}
				}
			}
			saveState.putIntArray("BlockData", blockData);

			ListTag entityList = new ListTag();
			int entityRadiusSqr = 64 * 64;

			for (Entity entity : player.level().getAllEntities()) {
				if (entity instanceof Player) continue;

				if (entity.distanceToSqr(player) < entityRadiusSqr) {
					TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.level().registryAccess());

					entity.saveAsPassenger(output);
					CompoundTag data = output.buildResult();

					data.remove("UUID");

					entityList.add(data);
				}
			}
			saveState.put("Entities", entityList);
        })));

		ServerPlayNetworking.registerGlobalReceiver(LoadPayload.ID, ((payload, context) -> context.server().execute(() -> {
			var server = context.server();
			ServerPlayer player = server.getPlayerList().getPlayers().getFirst();

			int[] blockData = saveState.getIntArray("BlockData").get();
			int anchorX = saveState.getIntOr("anchorX", 0);
			int anchorY = saveState.getIntOr("anchorY", 0);
			int anchorZ = saveState.getIntOr("anchorZ", 0);
			int radius = saveState.getIntOr("radius", 0);

			int size = (radius * 2 + 1);
			int index = 0;

			for (int y = 0; y < size; y++) {
				for (int z = 0; z < size; z++) {
					for (int x = 0; x < size; x++) {
						BlockPos pos = new BlockPos(anchorX + x, anchorY + y, anchorZ + z);
						BlockState savedState = Block.stateById(blockData[index++]);

						player.level().setBlock(pos, savedState, 2 | 16);
					}
				}
			}

			ServerLevel world = player.level();
			int entityRadiusSqr = 64 * 64;
			List<Entity> toDiscard = new ArrayList<>();

			for (Entity e : world.getAllEntities()) {
				if (!(e instanceof Player) && e.distanceToSqr(player) < entityRadiusSqr) {
					toDiscard.add(e);
				}
			}
			toDiscard.forEach(Entity::discard);

			ListTag entityList = saveState.getListOrEmpty("Entities");

			for (int i = 0; i < entityList.size(); i++) {
				CompoundTag entityData = entityList.getCompoundOrEmpty(i);
				ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), entityData);

				EntityType.loadEntityRecursive(entityData, world, EntitySpawnReason.LOAD, (loadedEntity) -> {
					loadedEntity.load(input);

					world.addFreshEntity(loadedEntity);
					return loadedEntity;
				});
			}

			for (int i = 0; i < playerDataListTags.size(); i++) {
				CompoundTag currentPlayer = playerDataListTags.getCompoundOrEmpty(i);
				String stringUUID = String.valueOf(currentPlayer.get("playerUUID"));

				UUID playerUUID = UUID.fromString(stringUUID.substring(1, stringUUID.length() - 1));

				ServerPlayer currentServerPlayer = server.getPlayerList().getPlayer(playerUUID);


				CompoundTag playerData = currentPlayer.getCompoundOrEmpty("FullPlayerData");

				ValueInput playerInput = TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), playerData);
				currentServerPlayer.load(playerInput);

				currentServerPlayer.teleportTo(currentServerPlayer.level(), currentPlayer.getDouble("playerX").get(), currentPlayer.getDouble("playerY").get(), currentPlayer.getDouble("playerZ").get(), java.util.Set.of(), currentPlayer.getFloat("playerYaw").get(), currentPlayer.getFloat("playerPitch").get(), true);
			}

			long savedTime = saveState.getLongOr("SavedDayTime", world.getGameTime());
			world.clockManager().setTotalTicks(world.registryAccess().get(WorldClocks.OVERWORLD).get(), savedTime);
		})));
	}
}