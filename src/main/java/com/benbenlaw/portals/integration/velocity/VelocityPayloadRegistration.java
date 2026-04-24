package com.benbenlaw.portals.integration.velocity;

import com.benbenlaw.portals.Portals;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Portals.MOD_ID)
public final class VelocityPayloadRegistration {

    private VelocityPayloadRegistration() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(
                VelocityConnectPayload.TYPE,
                VelocityConnectPayload.STREAM_CODEC,
                (payload, context) -> {}
        );
        registrar.playToClient(
                VelocityFlushWaypointsPayload.TYPE,
                VelocityFlushWaypointsPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(VelocityClientWaypointFlushCompat::flushClientWaypointsAndAck)
        );
        registrar.playToServer(
                VelocityFlushAckPayload.TYPE,
                VelocityFlushAckPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        VelocityBridge.markClientWaypointsFlushed(context.player().getUUID()))
        );
    }
}
