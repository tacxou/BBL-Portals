package com.benbenlaw.portals.integration.jei;

import com.benbenlaw.portals.util.PortalIgnitionSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

public record PortalRecipe(

        Identifier fromDimension,
        Identifier toDimension,
        int portalWidth,
        int portalHeight,
        BlockState portalFrame,
        BlockState portalBlock,
        Identifier frameTester,
        PortalIgnitionSource ignitionSource
) {}
