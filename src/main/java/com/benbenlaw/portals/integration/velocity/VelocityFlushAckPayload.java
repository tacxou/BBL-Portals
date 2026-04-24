package com.benbenlaw.portals.integration.velocity;

import com.benbenlaw.portals.Portals;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record VelocityFlushAckPayload() implements CustomPacketPayload {

    public static final VelocityFlushAckPayload INSTANCE = new VelocityFlushAckPayload();

    public static final Type<VelocityFlushAckPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Portals.MOD_ID, "velocity_flush_ack")
    );

    public static final StreamCodec<ByteBuf, VelocityFlushAckPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {},
            buf -> INSTANCE
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
