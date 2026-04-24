package com.benbenlaw.portals.integration.velocity;

import com.benbenlaw.portals.Portals;
import com.benbenlaw.portals.integration.ftbchunks.FtbChunksLogoutCompat;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class VelocityBridge {

    private VelocityBridge() {}

    public static void sendPlayerToServer(ServerPlayer player, String serverName) {
        if (player == null || serverName == null || serverName.isEmpty()) {
            Portals.LOGGER.warn("DEBUG PORTALS: sendPlayerToServer ignoré, player ou serverName invalide.");
            return;
        }
        try {
            Portals.LOGGER.warn("DEBUG PORTALS: envoi VelocityConnectPayload pour {} vers {}.", player.getGameProfile().getName(), serverName);
            FtbChunksLogoutCompat.syncForTransfer(player);
            PacketDistributor.sendToPlayer(player, new VelocityConnectPayload(serverName));
            Portals.LOGGER.warn("DEBUG PORTALS: payload Velocity envoyé pour {}.", player.getGameProfile().getName());
        } catch (Exception e) {
            Portals.LOGGER.warn("Failed to send Velocity Connect payload for player {} to server '{}'",
                    player.getGameProfile().getName(), serverName, e);
        }
    }
}
