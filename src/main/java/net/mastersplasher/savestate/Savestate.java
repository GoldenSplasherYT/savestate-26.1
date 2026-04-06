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

			int radius = 50;
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
			ServerLevel world = player.level();
			int entityRadius = 64;

			for (Entity entity : world.getAllEntities()) {
				if (entity instanceof Player) {
					continue;
				}

				if (entity.distanceToSqr(player) < (entityRadius * entityRadius)) {
					TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, world.registryAccess());
					entity.saveWithoutId(output);

					entityList.add(output.buildResult());
				}
			}
			saveState.put("Entities", entityList);

//            try {
//                save(server);
//            } catch (IOException e) {
//				LOGGER.error("Error Running Save Method: ", e);
//            }
        })));

		ServerPlayNetworking.registerGlobalReceiver(LoadPayload.ID, ((payload, context) -> context.server().execute(() -> {
			var server = context.server();
			ServerPlayer player = server.getPlayerList().getPlayers().getFirst();

			player.teleportTo(
					player.level(),
					saveState.getDouble("playerX").get(),
					saveState.getDouble("playerY").get(),
					saveState.getDouble("playerZ").get(),
					java.util.Set.of(),
					saveState.getFloat("playerYaw").get(),
					saveState.getFloat("playerPitch").get(),
					true
			);

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
			int entityRadius = 64;

			ListTag entityList = saveState.getListOrEmpty("Entities");

			for (int i = 0; i < entityList.size(); i++) {
				CompoundTag entityData = (CompoundTag) entityList.get(i);

				TagValueInput input = (TagValueInput) TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), entityData);

				EntityType.loadEntityRecursive(entityData, world, EntitySpawnReason.LOAD, (loadedEntity) -> {
					loadedEntity.load(input);

					world.addFreshEntity(loadedEntity);
					return loadedEntity;
				});
			}

			for (Entity entity : world.getAllEntities()) {
				if (!(entity instanceof Player) && entity.distanceToSqr(player) < (entityRadius * entityRadius)) {
					entity.discard(); // Remove them from the world instantly
				}
			}

//			loadSave(server);
		})));
	}
}