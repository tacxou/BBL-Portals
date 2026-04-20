package com.benbenlaw.portals.integration.kubejs;

import com.benbenlaw.portals.integration.velocity.VelocityBridge;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.server.level.ServerPlayer;

public final class PortalsJSBindings {

    private PortalsJSBindings() {}

    @Info("Transfer a player to another Velocity backend server by name. The server must sit behind a Velocity or BungeeCord proxy; otherwise the call is a no-op.")
    public static void sendToServer(ServerPlayer player, String serverName) {
        VelocityBridge.sendPlayerToServer(player, serverName);
    }
}
