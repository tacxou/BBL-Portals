package com.benbenlaw.portals.integration.velocity;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record VelocityConnectPayload(String serverName) implements CustomPacketPayload {

    public static final Type<VelocityConnectPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("bungeecord", "main")
    );

    public static final StreamCodec<ByteBuf, VelocityConnectPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (DataOutputStream out = new DataOutputStream(baos)) {
                    out.writeUTF("Connect");
                    out.writeUTF(payload.serverName);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to encode Velocity Connect payload", e);
                }
                buf.writeBytes(baos.toByteArray());
            },
            buf -> {
                buf.skipBytes(buf.readableBytes());
                return new VelocityConnectPayload("");
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
