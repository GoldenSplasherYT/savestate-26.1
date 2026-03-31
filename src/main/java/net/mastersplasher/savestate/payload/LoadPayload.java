package net.mastersplasher.savestate.payload;

import net.mastersplasher.savestate.Savestate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record LoadPayload() implements CustomPacketPayload {
    public static final Type<LoadPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Savestate.MOD_ID, "load_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LoadPayload> CODEC = StreamCodec.unit(new LoadPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
