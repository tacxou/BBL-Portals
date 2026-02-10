package com.benbenlaw.portals.util;

import com.benbenlaw.portals.mixin.client.ChunkRendererRegionAccessor;
import com.benbenlaw.portals.mixin.client.LevelSliceAccessor;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

public interface IColored {

    class BlockColors implements BlockColor {

        @Override
        public int getColor(BlockState state,
                            @Nullable BlockAndTintGetter tintGetter,
                            @Nullable BlockPos pos,
                            int tintIndex) {

            if (pos == null || tintGetter == null) {
                return 0xFFFFFF;
            }

            Level level = null;

            // Vanilla
            if (tintGetter instanceof ChunkRendererRegionAccessor vanilla) {
                level = vanilla.getLevel();
            }
            // Sodium
            else if (ModList.get().isLoaded("sodium")) {
                if (tintGetter instanceof LevelSliceAccessor sodium) {
                    level = sodium.getLevel();
                }
            }

            if (level != null) {
                Block block = CustomPortalHelper.getPortalBase(level, pos);
                PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(block);
                if (link != null) return link.colorID;
            }

            return 0xFFFFFF;
        }

        /*

        @Override
        public int getColor(BlockState state, @Nullable BlockAndTintGetter tintGetter, @Nullable BlockPos pos, int tintIndex) {
            if (pos != null && tintGetter instanceof RenderChunkRegion) {
                Block block = CustomPortalHelper.getPortalBase(((ChunkRendererRegionAccessor) tintGetter).getLevel(), pos);
                PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(block);
                if (link != null) return link.colorID;
            }
            return 0xFFFFFF;
        }

         */

    }
}