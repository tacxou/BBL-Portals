package com.benbenlaw.portals.integration.velocity;

import com.benbenlaw.portals.Portals;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;

public record VelocityTransferSyncPayload(
        String serverName,
        long portalPos,
        ResourceLocation frameBlockId,
        Direction.Axis axis,
        ResourceLocation dimensionId,
        boolean allowPortalCreation
) implements CustomPacketPayload {

    public static final Type<VelocityTransferSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Portals.MOD_ID, "velocity_transfer_sync")
    );

    public static final StreamCodec<ByteBuf, VelocityTransferSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                writeString(buf, payload.serverName);
                buf.writeLong(payload.portalPos);
                writeString(buf, payload.frameBlockId.toString());
                writeString(buf, payload.axis.getName());
                writeString(buf, payload.dimensionId.toString());
                buf.writeBoolean(payload.allowPortalCreation);
            },
            buf -> {
                String serverName = readString(buf);
                long portalPos = buf.readLong();
                ResourceLocation frameBlockId = ResourceLocation.parse(readString(buf));
                String axisName = readString(buf);
                Direction.Axis axis = Direction.Axis.byName(axisName);
                if (axis == null) {
                    axis = Direction.Axis.X;
                }
                ResourceLocation dimensionId = ResourceLocation.parse(readString(buf));
                boolean allowPortalCreation = buf.readBoolean();
                return new VelocityTransferSyncPayload(serverName, portalPos, frameBlockId, axis, dimensionId, allowPortalCreation);
            }
    );

    private static void writeString(ByteBuf buf, String value) {
        byte[] bytes = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    private static String readString(ByteBuf buf) {
        int length = buf.readInt();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
