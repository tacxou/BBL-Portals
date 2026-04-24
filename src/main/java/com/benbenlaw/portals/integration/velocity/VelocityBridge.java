package com.benbenlaw.portals.integration.velocity;

import com.benbenlaw.portals.Portals;
import com.benbenlaw.portals.integration.ftbchunks.FtbChunksLogoutCompat;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Portals.MOD_ID)
public final class VelocityBridge {

    private static final int TRANSFER_DELAY_TICKS = 20;
    private static final Map<UUID, PendingTransfer> PENDING_TRANSFERS = new ConcurrentHashMap<>();

    private VelocityBridge() {}

    public static void sendPlayerToServer(ServerPlayer player, String serverName) {
        if (player == null || serverName == null || serverName.isEmpty()) {
            Portals.LOGGER.warn("Transfert Velocity ignoré: player ou serverName invalide.");
            return;
        }
        try {
            FtbChunksLogoutCompat.syncForTransfer(player);
            PENDING_TRANSFERS.put(player.getUUID(), new PendingTransfer(serverName, TRANSFER_DELAY_TICKS));
            Portals.LOGGER.info(
                    "Transfert Velocity planifié pour {} vers {} dans {} ticks.",
                    player.getGameProfile().getName(),
                    serverName,
                    TRANSFER_DELAY_TICKS
            );
        } catch (Exception e) {
            Portals.LOGGER.warn("Failed to send Velocity Connect payload for player {} to server '{}'",
                    player.getGameProfile().getName(), serverName, e);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (PENDING_TRANSFERS.isEmpty()) {
            return;
        }

        PENDING_TRANSFERS.entrySet().removeIf(entry -> {
            UUID playerId = entry.getKey();
            PendingTransfer transfer = entry.getValue();

            if (transfer.remainingTicks > 0) {
                transfer.remainingTicks--;
                return false;
            }

            ServerPlayer player = event.getServer().getPlayerList().getPlayer(playerId);
            if (player == null) {
                return true;
            }

            try {
                PacketDistributor.sendToPlayer(player, new VelocityConnectPayload(transfer.serverName));
                Portals.LOGGER.info("Payload Velocity envoyé pour {} vers {}.", player.getGameProfile().getName(), transfer.serverName);
            } catch (Exception e) {
                Portals.LOGGER.warn("Failed to send delayed Velocity payload for player {} to server '{}'",
                        player.getGameProfile().getName(), transfer.serverName, e);
            }
            return true;
        });
    }

    private static final class PendingTransfer {
        private final String serverName;
        private int remainingTicks;

        private PendingTransfer(String serverName, int remainingTicks) {
            this.serverName = serverName;
            this.remainingTicks = remainingTicks;
        }
    }
}
