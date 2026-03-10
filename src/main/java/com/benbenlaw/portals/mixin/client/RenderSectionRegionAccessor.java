package com.benbenlaw.portals.mixin.client;

import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSectionRegion.class)
public interface RenderSectionRegionAccessor {

    @Accessor("level")
    Level getLevel();
}