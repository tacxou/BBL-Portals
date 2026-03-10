package com.benbenlaw.portals.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.HashSet;
import java.util.Optional;
import java.util.function.BiFunction;

public class PortalIgnitionSource {

    public static final PortalIgnitionSource FIRE = new PortalIgnitionSource(
        SourceType.BLOCKPLACED,
        BuiltInRegistries.BLOCK.getKey(Blocks.FIRE)
    );

    public static final PortalIgnitionSource WATER = FluidSource(Fluids.WATER);

    public enum SourceType {
        USEITEM,
        BLOCKPLACED,
        FLUID,
        CUSTOM
    }

    private static final HashSet<Item> USEITEMS = new HashSet<>();

    public SourceType sourceType;

    public Identifier ignitionSourceID;

    public Player player;

    private PortalIgnitionSource(SourceType sourceType, Identifier ignitionSourceID) {
        this.sourceType = sourceType;
        this.ignitionSourceID = ignitionSourceID;
    }

    public PortalIgnitionSource withPlayer(Player player) {
        this.player = player;
        return this;
    }

    public static PortalIgnitionSource ItemUseSource(Item item) {
        USEITEMS.add(item);
        return new PortalIgnitionSource(SourceType.USEITEM, BuiltInRegistries.ITEM.getKey(item));
    }

    public static PortalIgnitionSource FluidSource(Fluid fluid) {
        return new PortalIgnitionSource(SourceType.FLUID, BuiltInRegistries.FLUID.getKey(fluid));
    }

    public static PortalIgnitionSource CustomSource(Identifier ignitionSourceID) {
        return new PortalIgnitionSource(SourceType.CUSTOM, ignitionSourceID);
    }

    public static boolean isRegisteredIgnitionSourceWith(Item item) {
        return USEITEMS.contains(item);
    }

    // TODO: implement
    @Deprecated
    public void withCondition(BiFunction<Level, BlockPos, Boolean> condition) {}

    public boolean isWater() {
        return Optional.of(BuiltInRegistries.FLUID.getValue(ignitionSourceID))
            .filter(
                a -> a.is(FluidTags.WATER)
            )
            .isPresent();
    }

    public boolean isLava() {
        return Optional.of(BuiltInRegistries.FLUID.getValue(ignitionSourceID))
            .filter(
                a -> a.is(FluidTags.LAVA)
            )
            .isPresent();
    }

    public ItemStack getIgnetionItemStack() {
        if (sourceType == SourceType.USEITEM) {
            return new ItemStack(BuiltInRegistries.ITEM.getValue(ignitionSourceID));
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getFluidIgnitionAsBucket() {
        if (sourceType == SourceType.FLUID) {
            Fluid fluid = BuiltInRegistries.FLUID.getValue(ignitionSourceID);
            return fluid.getBucket().getDefaultInstance();
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getBlockPlacedAsItemStack() {
        if (sourceType == SourceType.BLOCKPLACED) {
            return new ItemStack(BuiltInRegistries.BLOCK.getValue(ignitionSourceID).asItem());
        }
        return ItemStack.EMPTY;
    }
}