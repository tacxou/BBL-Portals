package com.benbenlaw.portals.integration.velocity;

import com.benbenlaw.portals.Portals;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record VelocityFlushWaypointsPayload() implements CustomPacketPayload {

    public static final VelocityFlushWaypointsPayload INSTANCE = new VelocityFlushWaypointsPayload();

    public static final Type<VelocityFlushWaypointsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Portals.MOD_ID, "velocity_flush_waypoints")
    );

    public static final StreamCodec<ByteBuf, VelocityFlushWaypointsPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {},
            buf -> INSTANCE
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
