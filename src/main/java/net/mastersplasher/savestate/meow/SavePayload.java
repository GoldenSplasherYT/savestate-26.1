package net.mastersplasher.savestate.meow;

import net.mastersplasher.savestate.Savestate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SavePayload() implements CustomPacketPayload {
    public static final Type<SavePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Savestate.MOD_ID, "save_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SavePayload> CODEC = StreamCodec.unit(new SavePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
