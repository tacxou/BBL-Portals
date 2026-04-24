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

    private static final int ACK_TIMEOUT_TICKS = 120;
    private static final Map<UUID, PendingTransfer> PENDING_TRANSFERS = new ConcurrentHashMap<>();

    private VelocityBridge() {}

    public static void sendPlayerToServer(ServerPlayer player, String serverName) {
        if (player == null || serverName == null || serverName.isEmpty()) {
            Portals.LOGGER.warn("Transfert Velocity ignoré: player ou serverName invalide.");
            return;
        }
        try {
            PENDING_TRANSFERS.put(player.getUUID(), new PendingTransfer(serverName, ACK_TIMEOUT_TICKS));
            PacketDistributor.sendToPlayer(player, VelocityFlushWaypointsPayload.INSTANCE);
            Portals.LOGGER.info(
                    "Flush waypoints demandé pour {} avant transfert vers {} (timeout: {} ticks).",
                    player.getGameProfile().getName(),
                    serverName,
                    ACK_TIMEOUT_TICKS
            );
        } catch (Exception e) {
            Portals.LOGGER.warn("Failed to send Velocity Connect payload for player {} to server '{}'",
                    player.getGameProfile().getName(), serverName, e);
        }
    }

    public static void markClientWaypointsFlushed(UUID playerId) {
        PendingTransfer transfer = PENDING_TRANSFERS.get(playerId);
        if (transfer != null) {
            transfer.clientFlushed = true;
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

            ServerPlayer player = event.getServer().getPlayerList().getPlayer(playerId);
            if (player == null) {
                return true;
            }

            if (!transfer.clientFlushed) {
                if (transfer.remainingTicks > 0) {
                    transfer.remainingTicks--;
                    return false;
                }
                Portals.LOGGER.warn("Timeout flush waypoints pour {}, transfert Velocity forcé.", player.getGameProfile().getName());
            }

            try {
                FtbChunksLogoutCompat.syncForTransfer(player);
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
        private boolean clientFlushed;

        private PendingTransfer(String serverName, int remainingTicks) {
            this.serverName = serverName;
            this.remainingTicks = remainingTicks;
            this.clientFlushed = false;
        }
    }
}
