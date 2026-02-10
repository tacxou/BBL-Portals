package com.benbenlaw.portals.event;

import com.benbenlaw.portals.util.PortalIgnitionSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface PortalIgniteEvent {

    void afterLight(Player player, Level world, BlockPos portalPos, BlockPos framePos, PortalIgnitionSource portalIgnitionSource);
}