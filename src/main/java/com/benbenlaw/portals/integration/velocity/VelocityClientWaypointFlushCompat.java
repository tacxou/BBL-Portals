package com.benbenlaw.portals.integration.velocity;

import com.benbenlaw.portals.Portals;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Method;
import java.util.Optional;

public final class VelocityClientWaypointFlushCompat {

    private static final String FTB_CHUNKS_MOD = "ftbchunks";
    private static VelocityTransferArrivalPayload pendingArrivalPayload;

    private VelocityClientWaypointFlushCompat() {}

    public static void cacheTransfer(VelocityTransferSyncPayload payload) {
        pendingArrivalPayload = new VelocityTransferArrivalPayload(
                payload.portalPos(),
                payload.frameBlockId(),
                payload.axis(),
                payload.dimensionId(),
                payload.allowPortalCreation()
        );
    }

    public static VelocityTransferArrivalPayload consumePendingArrivalPayload() {
        VelocityTransferArrivalPayload payload = pendingArrivalPayload;
        pendingArrivalPayload = null;
        return payload;
    }

    public static void flushClientWaypointsAndAck() {
        if (!ModList.get().isLoaded(FTB_CHUNKS_MOD)) {
            PacketDistributor.sendToServer(VelocityFlushAckPayload.INSTANCE);
            return;
        }

        try {
            Class<?> mapManagerClass = Class.forName("dev.ftb.mods.ftbchunks.client.map.MapManager");
            Method getInstance = mapManagerClass.getMethod("getInstance");
            Optional<?> managerOpt = (Optional<?>) getInstance.invoke(null);
            if (managerOpt.isPresent()) {
                Object manager = managerOpt.get();
                Method saveAllRegions = manager.getClass().getMethod("saveAllRegions");
                saveAllRegions.invoke(manager);
                Portals.LOGGER.info("FTB Chunks: sauvegarde client forcée avant transfert Velocity.");
            } else {
                Portals.LOGGER.warn("FTB Chunks: MapManager client indisponible, flush ignoré.");
            }
        } catch (Throwable t) {
            Portals.LOGGER.warn("FTB Chunks: échec du flush client avant transfert Velocity.", t);
        }

        PacketDistributor.sendToServer(VelocityFlushAckPayload.INSTANCE);
    }
}
