package com.benbenlaw.portals.integration.velocity;

import com.benbenlaw.portals.Portals;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Portals.MOD_ID, value = Dist.CLIENT)
public final class VelocityClientTransferSync {

    private VelocityClientTransferSync() {}

    @SubscribeEvent
    public static void onClientLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        VelocityTransferArrivalPayload pendingArrivalPayload = VelocityClientWaypointFlushCompat.consumePendingArrivalPayload();
        if (pendingArrivalPayload == null) {
            return;
        }
        PacketDistributor.sendToServer(pendingArrivalPayload);
    }
}
