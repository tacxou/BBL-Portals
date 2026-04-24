package com.benbenlaw.portals.integration.velocity;

import com.benbenlaw.portals.Portals;
import com.benbenlaw.portals.portal.PortalPlacer;
import com.benbenlaw.portals.portal.frame.PortalFrameTester;
import com.benbenlaw.portals.integration.ftbchunks.FtbChunksLogoutCompat;
import com.benbenlaw.portals.util.CustomPortalApiRegistry;
import com.benbenlaw.portals.util.PortalLink;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Portals.MOD_ID)
public final class VelocityBridge {

    private static final int ACK_TIMEOUT_TICKS = 120;
    private static final Map<UUID, PendingTransfer> PENDING_TRANSFERS = new ConcurrentHashMap<>();

    private VelocityBridge() {}

    public static void sendPlayerToServer(ServerPlayer player, String serverName) {
        if (player == null) {
            return;
        }
        BlockPos playerPos = player.blockPosition();
        Block frameBlock = player.level().getBlockState(playerPos.below()).getBlock();
        sendPlayerToServer(player, serverName, playerPos, frameBlock, Direction.Axis.X, player.level().dimension().location());
    }

    public static void sendPlayerToServer(ServerPlayer player, String serverName, BlockPos portalPos, Block portalBase, Direction.Axis portalAxis) {
        sendPlayerToServer(player, serverName, portalPos, portalBase, portalAxis, player.level().dimension().location());
    }

    public static void sendPlayerToServer(
            ServerPlayer player,
            String serverName,
            BlockPos portalPos,
            Block portalBase,
            Direction.Axis portalAxis,
            ResourceLocation targetDimensionId
    ) {
        if (player == null || serverName == null || serverName.isEmpty()) {
            Portals.LOGGER.warn("Transfert Velocity ignoré: player ou serverName invalide.");
            return;
        }
        try {
            ResourceLocation frameBlockId = BuiltInRegistries.BLOCK.getKey(portalBase);
            PortalLink sourceLink = CustomPortalApiRegistry.getPortalLinkFromBase(portalBase);
            boolean allowPortalCreation = sourceLink != null && sourceLink.allowInterServerPortalCreation;
            PENDING_TRANSFERS.put(
                    player.getUUID(),
                    new PendingTransfer(
                            serverName,
                            ACK_TIMEOUT_TICKS
                    )
            );
            PacketDistributor.sendToPlayer(player, new VelocityTransferSyncPayload(
                    serverName,
                    portalPos.asLong(),
                    frameBlockId,
                    portalAxis,
                    targetDimensionId != null ? targetDimensionId : player.level().dimension().location(),
                    allowPortalCreation
            ));
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

    public static void handleInterServerArrival(ServerPlayer player, VelocityTransferArrivalPayload payload) {
        if (player == null) {
            return;
        }

        ResourceKey<Level> targetDimension = ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION,
                payload.dimensionId()
        );
        ServerLevel level = player.server.getLevel(targetDimension);
        if (level == null) {
            level = player.serverLevel();
        }

        Block frameBlock = BuiltInRegistries.BLOCK.getOptional(payload.frameBlockId()).orElse(null);
        if (frameBlock == null) {
            Portals.LOGGER.warn("Transfert inter-serveur: bloc de frame introuvable {}.", payload.frameBlockId());
            return;
        }

        PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(frameBlock);
        if (link == null) {
            Portals.LOGGER.warn("Transfert inter-serveur: aucun PortalLink pour {}.", payload.frameBlockId());
            return;
        }

        BlockPos requestedPos = BlockPos.of(payload.portalPos());
        if (level.dimension().equals(Level.END)) {
            requestedPos = dragonIslandPortalPos(level);
        }
        BlockPos targetPortalPos = requestedPos;
        GlobalPos linked = Portals.PORTAL_LINKING_STORAGE.getDestination(requestedPos, level.dimension());
        if (linked != null && linked.dimension().equals(level.dimension())) {
            targetPortalPos = linked.pos();
        }

        BlockUtil.FoundRectangle rectangle = findExistingPortalNear(level, link, frameBlock, targetPortalPos, payload.axis());
        if (rectangle == null && !targetPortalPos.equals(requestedPos)) {
            rectangle = findExistingPortalNear(level, link, frameBlock, requestedPos, payload.axis());
        }

        if (rectangle == null && !payload.allowPortalCreation()) {
            Portals.LOGGER.info(
                    "Transfert inter-serveur: création désactivée pour {} (frame {}).",
                    player.getGameProfile().getName(),
                    payload.frameBlockId()
            );
            Portals.LOGGER.info("Transfert inter-serveur: aucun portail existant trouvé près de {}.", requestedPos);
            return;
        }

        if (rectangle == null) {
            BlockPos portalCreationPos = requestedPos;
            if (payload.allowPortalCreation()) {
                portalCreationPos = adaptToWaterSurface(level, requestedPos);
                buildSafetyPlatform(level, portalCreationPos);
            }
            Optional<BlockUtil.FoundRectangle> created = PortalPlacer.createDestinationPortal(
                    level,
                    portalCreationPos,
                    frameBlock.defaultBlockState(),
                    payload.axis()
            );
            if (created.isEmpty()) {
                Portals.LOGGER.warn("Transfert inter-serveur: impossible de créer le portail pour {}.", player.getGameProfile().getName());
                return;
            }
            rectangle = created.get();
        }

        if (!rectangle.minCorner.equals(requestedPos)) {
            Portals.PORTAL_LINKING_STORAGE.createLink(
                    requestedPos,
                    level.dimension(),
                    rectangle.minCorner,
                    level.dimension()
            );
        }

        Vec3 destination = new Vec3(
                rectangle.minCorner.getX() + 0.5D,
                rectangle.minCorner.getY() + 1D,
                rectangle.minCorner.getZ() + 0.5D
        );
        player.teleportTo(level, destination.x, destination.y, destination.z, player.getYRot(), player.getXRot());
        Portals.LOGGER.info(
                "Transfert inter-serveur: portail synchronisé pour {} vers {}.",
                player.getGameProfile().getName(),
                destination
        );
    }

    private static BlockPos dragonIslandPortalPos(ServerLevel level) {
        BlockPos base = ServerLevel.END_SPAWN_POINT;
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base.getX(), base.getZ());
        return new BlockPos(base.getX(), Math.max(y, base.getY()), base.getZ());
    }

    private static BlockUtil.FoundRectangle findExistingPortalNear(
            ServerLevel level,
            PortalLink link,
            Block frameBlock,
            BlockPos center,
            Direction.Axis preferredAxis
    ) {
        Direction.Axis secondaryAxis = preferredAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        for (int radius = 0; radius <= 12; radius++) {
            for (BlockPos.MutableBlockPos mutable : BlockPos.spiralAround(center, radius, Direction.EAST, Direction.SOUTH)) {
                for (int y = center.getY() + 4; y >= center.getY() - 4; y--) {
                    BlockPos candidate = mutable.immutable().atY(y);
                    BlockUtil.FoundRectangle rect = validatePortalAt(level, link, frameBlock, candidate, preferredAxis);
                    if (rect != null) {
                        return rect;
                    }
                    rect = validatePortalAt(level, link, frameBlock, candidate, secondaryAxis);
                    if (rect != null) {
                        return rect;
                    }
                }
            }
        }
        return null;
    }

    private static BlockUtil.FoundRectangle validatePortalAt(
            ServerLevel level,
            PortalLink link,
            Block frameBlock,
            BlockPos pos,
            Direction.Axis axis
    ) {
        PortalFrameTester tester = link.getFrameTester()
                .createInstanceOfPortalFrameTester()
                .init(level, pos, axis, frameBlock);
        if (!tester.isValidFrame()) {
            return null;
        }
        if (!tester.isAlreadyLitPortalFrame()) {
            tester.lightPortal(frameBlock);
        }
        return tester.getRectangle();
    }

    private static BlockPos adaptToWaterSurface(ServerLevel level, BlockPos pos) {
        int x = pos.getX();
        int z = pos.getZ();
        int worldSurfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
        int minY = level.getMinBuildHeight();
        if (worldSurfaceY < minY) {
            return pos;
        }

        int firstWaterY = Integer.MIN_VALUE;
        for (int y = worldSurfaceY; y >= minY; y--) {
            BlockPos checkPos = new BlockPos(x, y, z);
            if (level.getBlockState(checkPos).getFluidState().is(Fluids.WATER)) {
                firstWaterY = y;
                break;
            }
        }

        if (firstWaterY == Integer.MIN_VALUE) {
            return pos;
        }

        int topWaterY = firstWaterY;
        while (topWaterY + 1 <= worldSurfaceY) {
            BlockPos above = new BlockPos(x, topWaterY + 1, z);
            if (!level.getBlockState(above).getFluidState().is(Fluids.WATER)) {
                break;
            }
            topWaterY++;
        }

        return new BlockPos(x, topWaterY + 1, z);
    }

    private static void buildSafetyPlatform(ServerLevel level, BlockPos center) {
        Block platformBlock = getPlatformBlockForDimension(level);
        int platformRadius = 6;
        int depth = 2;

        for (int dx = -platformRadius; dx <= platformRadius; dx++) {
            for (int dz = -platformRadius; dz <= platformRadius; dz++) {
                if (dx * dx + dz * dz > platformRadius * platformRadius) {
                    continue;
                }

                for (int d = 1; d <= depth; d++) {
                    BlockPos current = center.offset(dx, -d, dz);
                    level.setBlockAndUpdate(current, platformBlock.defaultBlockState());
                }
            }
        }
    }

    private static Block getPlatformBlockForDimension(ServerLevel level) {
        if (level.dimension().equals(Level.NETHER)) {
            return Blocks.NETHERRACK;
        }
        if (level.dimension().equals(Level.END)) {
            return Blocks.END_STONE;
        }
        return Blocks.BLUE_ICE;
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

        private PendingTransfer(
                String serverName,
                int remainingTicks
        ) {
            this.serverName = serverName;
            this.remainingTicks = remainingTicks;
            this.clientFlushed = false;
        }
    }
}
