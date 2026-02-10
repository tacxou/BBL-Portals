package com.benbenlaw.portals.block;

import com.benbenlaw.portals.Portals;
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
                            .noCollission()
                            .strength(-1)
                            .sound(SoundType.GLASS)
                            .lightLevel(state -> 11)
                            .pushReaction(PushReaction.BLOCK)
            )
    );


    private static <T extends Block> DeferredBlock<T> registerBlockWithoutBlockItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

}
