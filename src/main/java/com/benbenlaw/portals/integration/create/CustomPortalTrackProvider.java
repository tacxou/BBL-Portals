package com.benbenlaw.portals.integration.create;
import com.benbenlaw.portals.portal.linking.PortalLinkingStorage;
import com.simibubi.create.Create;
import com.simibubi.create.api.contraption.train.PortalTrackProvider;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.benbenlaw.portals.portal.linking.PortalLinkingStorage;
import com.simibubi.create.Create;
import com.simibubi.create.api.contraption.train.PortalTrackProvider;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.Map;

public class CustomPortalTrackProvider implements PortalTrackProvider {

    @Override
    public Exit findExit(ServerLevel level, BlockFace inboundTrack) {
        BlockPos portalPos = inboundTrack.getConnectedPos();
        BlockState portalState = level.getBlockState(portalPos);
        ResourceLocation currentDim = level.dimension().location();
        
        DimensionDataStorage dataStorage = level.getServer().overworld().getDataStorage();
        String[] possibleKeys = {"portal_links", "portals_portal_links", "portal_linking_storage", "portals"};

        PortalLinkingStorage storage = null;
        for (String key : possibleKeys) {
            storage = dataStorage.get(PortalLinkingStorage.factory(), key);
            if (storage != null && !storage.PORTAL_LINKS.isEmpty()) {
                System.out.println("Create-Portal-Compat: Found valid data using key: '{}'" + key);
                break;
            }
        }

        if (storage == null || storage.PORTAL_LINKS.isEmpty()) {
            System.out.println("Create-Portal-Compat: No portal data found. Is the portal actually linked and saved?");
            return null;
        }
        
        Map.Entry<ResourceLocation, java.util.concurrent.ConcurrentHashMap<BlockPos, GlobalPos>> dimEntry = null;
        for (var entry : storage.PORTAL_LINKS.entrySet()) {
            if (entry.getKey().toString().equals(currentDim.toString())) {
                dimEntry = entry;
                break;
            }
        }

        if (dimEntry == null) {
            System.out.println("Create-Portal-Compat: Current dim {} not found in: {}" + currentDim + storage.PORTAL_LINKS.keySet());
            return null;
        }

        GlobalPos destination = null;
        double closestDistSq = 1024; // 32 block radius

        for (Map.Entry<BlockPos, GlobalPos> entry : dimEntry.getValue().entrySet()) {
            double distSq = entry.getKey().distSqr(portalPos);
            if (distSq < closestDistSq) {
                closestDistSq = distSq;
                destination = entry.getValue();
            }
        }

        if (destination == null) return null;

        ServerLevel otherLevel = level.getServer().getLevel(destination.dimension());
        if (otherLevel == null) return null;

        BlockPos otherPortalPos = null;
        searchOther:
        for (int x = -10; x <= 10; x++) {
            for (int y = -15; y <= 15; y++) {
                for (int z = -10; z <= 10; z++) {
                    BlockPos checkPos = destination.pos().offset(x, y, z);
                    if (otherLevel.getBlockState(checkPos).is(portalState.getBlock())) {
                        otherPortalPos = checkPos;
                        break searchOther;
                    }
                }
            }
        }

        if (otherPortalPos == null) return null;

        Direction targetDirection = inboundTrack.getFace();
        BlockState otherPortalState = otherLevel.getBlockState(otherPortalPos);
        if (otherPortalState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
            if (targetDirection.getAxis() == otherPortalState.getValue(BlockStateProperties.HORIZONTAL_AXIS)) {
                targetDirection = targetDirection.getClockWise();
            }
        }

        return new Exit(otherLevel, new BlockFace(otherPortalPos.relative(targetDirection), targetDirection.getOpposite()));
    }
}