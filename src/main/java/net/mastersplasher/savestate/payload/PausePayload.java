package net.mastersplasher.savestate.payload;

import net.mastersplasher.savestate.Savestate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record PausePayload(boolean frozen) implements CustomPacketPayload {
    public static final Type<@NotNull PausePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Savestate.MOD_ID, "pause_payload"));

    public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull PausePayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeBoolean(payload.frozen()), // Encoder
            buf -> new PausePayload(buf.readBoolean()) // Decoder
    );

    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return ID;
    }
}
