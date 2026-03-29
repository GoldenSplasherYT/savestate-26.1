package net.mastersplasher.savestate.Payload;

import net.mastersplasher.savestate.Savestate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PausePayload() implements CustomPacketPayload {
    public static final Type<PausePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Savestate.MOD_ID, "pause_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PausePayload> CODEC = StreamCodec.unit(new PausePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
