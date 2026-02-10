package com.benbenlaw.portals.util;

import com.benbenlaw.portals.block.PortalsBlocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

public class PortalsColorHandler {

    @SubscribeEvent
    public void registerBlockColors(final RegisterColorHandlersEvent.Block event) {
        event.register(new IColored.BlockColors(), PortalsBlocks.CUSTOM_PORTAL.get());
    }


}