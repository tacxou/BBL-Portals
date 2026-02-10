package com.benbenlaw.portals.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.caffeinemc.mods.sodium.client.world.LevelSlice.class)
public interface LevelSliceAccessor {

    @Accessor("level")
    ClientLevel getLevel();
}