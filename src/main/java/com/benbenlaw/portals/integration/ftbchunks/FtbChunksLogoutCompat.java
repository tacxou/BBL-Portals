package com.benbenlaw.portals.integration.ftbchunks;

import com.benbenlaw.portals.Portals;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.lang.reflect.Method;

/**
 * Lors d'une déconnexion (y compris transfert Velocity), FTB Chunks doit finaliser l'état client des waypoints.
 * On délègue à {@code FTBChunks#loggedOut(ServerPlayer)} si le mod est présent.
 */
@EventBusSubscriber(modid = Portals.MOD_ID)
public final class FtbChunksLogoutCompat {

    private static final String FTB_CHUNKS_MOD = "ftbchunks";
    private static final String FTB_CHUNKS_CLASS = "dev.ftb.mods.ftbchunks.FTBChunks";

    private FtbChunksLogoutCompat() {}

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!ModList.get().isLoaded(FTB_CHUNKS_MOD)) {
            Portals.LOGGER.info("Logout détecté mais FTB Chunks n'est pas chargé, synchro waypoints ignorée.");
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Portals.LOGGER.info("Logout détecté pour {}, tentative de synchro FTB Chunks.", player.getGameProfile().getName());
        invokeFtbChunksLoggedOut(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        syncForLogin(player);
    }

    public static void syncForTransfer(ServerPlayer player) {
        if (player == null) {
            Portals.LOGGER.warn("DEBUG PORTALS: syncForTransfer ignoré, player null.");
            return;
        }
        if (!ModList.get().isLoaded(FTB_CHUNKS_MOD)) {
            Portals.LOGGER.warn("DEBUG PORTALS: syncForTransfer ignoré, ftbchunks non chargé.");
            return;
        }
        Portals.LOGGER.warn("DEBUG PORTALS: transfert détecté pour {}, tentative de synchro FTB Chunks.", player.getGameProfile().getName());
        invokeFtbChunksLoggedOut(player);
    }

    public static void syncForLogin(ServerPlayer player) {
        if (player == null || !ModList.get().isLoaded(FTB_CHUNKS_MOD)) {
            return;
        }
        try {
            Class<?> apiClass = Class.forName("dev.ftb.mods.ftbchunks.api.FTBChunksAPI");
            Object api = apiClass.getMethod("api").invoke(null);
            Object manager = api.getClass().getMethod("getManager").invoke(api);
            Object teamData = manager.getClass().getMethod("getOrCreateData", ServerPlayer.class).invoke(manager, player);
            Method syncChunksToPlayer = teamData.getClass().getMethod("syncChunksToPlayer", ServerPlayer.class);
            syncChunksToPlayer.invoke(teamData, player);
            Portals.LOGGER.info("Synchro FTB Chunks forcée au login pour {}.", player.getGameProfile().getName());
        } catch (ReflectiveOperationException e) {
            Portals.LOGGER.warn("Impossible de forcer la synchro FTB Chunks au login pour {} : {}", player.getGameProfile().getName(), e.toString());
        } catch (Throwable t) {
            Portals.LOGGER.warn("Erreur inattendue pendant la synchro FTB Chunks au login de {}", player.getGameProfile().getName(), t);
        }
    }

    private static void invokeFtbChunksLoggedOut(ServerPlayer player) {
        try {
            Class<?> ftbChunks = Class.forName(FTB_CHUNKS_CLASS);
            Object instance = ftbChunks.getField("instance").get(null);
            if (instance == null) {
                Portals.LOGGER.warn("FTB Chunks est chargé mais son instance est nulle au logout de {}.", player.getGameProfile().getName());
                return;
            }
            Method loggedOut = ftbChunks.getMethod("loggedOut", ServerPlayer.class);
            loggedOut.invoke(instance, player);
            Portals.LOGGER.info("Synchro FTB Chunks logout effectuée pour {}.", player.getGameProfile().getName());
        } catch (ReflectiveOperationException e) {
            Portals.LOGGER.warn("Impossible d'appeler FTBChunks.loggedOut pour {} : {}", player.getGameProfile().getName(), e.toString());
        } catch (Throwable t) {
            Portals.LOGGER.warn("Erreur inattendue lors de la mise à jour FTB Chunks à la déconnexion de {}", player.getGameProfile().getName(), t);
        }
    }
}
