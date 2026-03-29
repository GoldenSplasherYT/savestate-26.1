package net.mastersplasher.savestate.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.BonusChestFeature;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BonusChestFeature.class)
public class BonusChestMixin {

    @ModifyArg(method = "place", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/RandomizableContainer;setBlockEntityLootTable(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft" +
                    "/util/RandomSource;Lnet/minecraft/core/BlockPos;Lnet/minecraft/resources/ResourceKey;)V"), index = 3)
    private ResourceKey<LootTable> modifyLootTable(ResourceKey<LootTable> lootTable) {
        return BuiltInLootTables.END_CITY_TREASURE;
    }
}
