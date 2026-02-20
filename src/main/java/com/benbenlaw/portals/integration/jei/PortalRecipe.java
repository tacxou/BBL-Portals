package com.benbenlaw.portals.integration.jei;

import com.benbenlaw.portals.util.PortalIgnitionSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public record PortalRecipe(

        ResourceLocation fromDimension,
        ResourceLocation toDimension,
        int portalWidth,
        int portalHeight,
        BlockState portalFrame,
        BlockState portalBlock,
        ResourceLocation frameTester,
        PortalIgnitionSource ignitionSource
) {}
