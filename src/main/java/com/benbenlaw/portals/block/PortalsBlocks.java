package com.benbenlaw.portals.block;

import com.benbenlaw.portals.Portals;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class PortalsBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Portals.MOD_ID);


    public static final Supplier<CustomPortalBlock> CUSTOM_PORTAL = BLOCKS.register(
            "custom_portal",
            () -> new CustomPortalBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL)
                            .noCollision()
                            .strength(-1)
                            .sound(SoundType.GLASS)
                            .lightLevel(state -> 11)
                            .pushReaction(PushReaction.BLOCK).setId(createID("custom_portal"))
            )
    );


    private static <T extends Block> DeferredBlock<T> registerBlockWithoutBlockItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    public static ResourceKey<Block> createID(String name) {
        return ResourceKey.create(Registries.BLOCK, Portals.identifier(name));
    }

}
