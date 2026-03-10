package com.benbenlaw.portals.util;

import com.benbenlaw.portals.Portals;
import com.benbenlaw.portals.block.PortalsBlocks;
import com.benbenlaw.portals.portal.frame.PortalFrameTester;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class CustomPortalApiRegistry {

    protected static final ConcurrentHashMap<Block, PortalLink> portals = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Identifier, PortalFrameTester.PortalFrameTesterFactory> PortalFrameTesters =
        new ConcurrentHashMap<>();

    private CustomPortalApiRegistry() {}

    public static PortalLink getPortalLinkFromBase(Block baseBlock) {
        if (baseBlock == null)
            return null;
        if (portals.containsKey(baseBlock))
            return portals.get(baseBlock);
        return null;
    }

    public static PortalLink getPortalFromPortalBlock(Block portalBlock) {
        if (portalBlock == null)
            return null;
        for (PortalLink link : portals.values()) {
            if (link.getPortalBlock().equals(portalBlock))
                return link;
        }
        return null;
    }

    public static boolean isRegisteredFrameBlock(BlockState blockState) {
        return portals.containsKey(blockState.getBlock());
    }

    public static Collection<PortalLink> getAllPortalLinks() {
        return portals.values();
    }

    public static void registerPortalFrameTester(
            Identifier frameTesterID,
        PortalFrameTester.PortalFrameTesterFactory createPortalFrameTester
    ) {
        PortalFrameTesters.put(frameTesterID, createPortalFrameTester);
    }

    public static PortalFrameTester.PortalFrameTesterFactory getPortalFrameTester(Identifier frameTesterID) {
        return PortalFrameTesters.getOrDefault(frameTesterID, null);
    }

    public static void addPortal(Block frameBlock, PortalLink link) {
        if (frameBlock == null)
            throw new RuntimeException("Frame block must not be null");
        if (link.getPortalBlock() == null)
            throw new RuntimeException("Portal block must not be null");
        if (link.portalIgnitionSource == null)
            throw new RuntimeException("Portal ignition source must not be null");
        if (link.dimID == null)
            throw new RuntimeException("Dimension is null");
        if (!Portals.DIMENSIONS.isEmpty() && !Portals.DIMENSIONS.containsKey(link.dimID))
            throw new RuntimeException("Dimension not found");
        if (PortalsBlocks.CUSTOM_PORTAL.get() == null)
            throw new RuntimeException("Built-in CustomPortalBlock is null");

        if (portals.containsKey(frameBlock) || frameBlock.equals(Blocks.OBSIDIAN)) {
            throw new RuntimeException(
                "A portal of the frame '" + frameBlock + "' is already registered"
            );
        } else {
            portals.put(frameBlock, link);
        }
    }

    public static ConcurrentHashMap<Block, PortalLink> getPortals () {
        return portals;
    }
}