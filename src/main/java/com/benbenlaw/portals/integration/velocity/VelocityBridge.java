package com.benbenlaw.portals.integration.velocity;

import com.benbenlaw.portals.Portals;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class VelocityBridge {

    private VelocityBridge() {}

    public static void sendPlayerToServer(ServerPlayer player, String serverName) {
        if (player == null || serverName == null || serverName.isEmpty()) {
            return;
        }
        try {
            PacketDistributor.sendToPlayer(player, new VelocityConnectPayload(serverName));
        } catch (Exception e) {
            Portals.LOGGER.warn("Failed to send Velocity Connect payload for player {} to server '{}'",
                    player.getGameProfile().getName(), serverName, e);
        }
    }
}
