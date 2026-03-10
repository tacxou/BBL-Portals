package com.benbenlaw.portals.integration.jade;

/*

import com.benbenlaw.portals.Portals;
import com.benbenlaw.portals.util.CustomPortalApiRegistry;
import com.benbenlaw.portals.util.CustomPortalHelper;
import com.benbenlaw.portals.util.PortalLink;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import snownee.jade.addon.core.ObjectNameProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class PortalNameProvider implements IBlockComponentProvider {

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor accessor, IPluginConfig iPluginConfig) {
        Level world = accessor.getLevel();
        BlockPos pos = accessor.getPosition();
        if (world == null || pos == null) {
            iTooltip.add(Component.literal("Portal"));
            return;
        }

        BlockPos framePos = CustomPortalHelper.getClosestFrameBlock(world, pos);
        Block frameBlock = CustomPortalHelper.getPortalBase(world, framePos);

        PortalLink link = frameBlock != Blocks.AIR ? CustomPortalApiRegistry.getPortalLinkFromBase(frameBlock) : null;
        if (link == null) {
            iTooltip.add(Component.literal("Portal"));
            return;
        }

        Identifier currentDim = accessor.getPlayer().level().dimension().location();
        Identifier destinationDim = currentDim.equals(link.dimID) && link.returnDimID != null ? link.returnDimID
                : (currentDim.equals(link.returnDimID) && link.dimID != null ? link.dimID
                : (link.dimID != null ? link.dimID : link.returnDimID));

        String formattedName = formatDimensionName(destinationDim);
        iTooltip.add(Component.translatable("tooltip.portal.jade.to", formattedName));
    }

    private String formatDimensionName(Identifier dim) {
        if (dim == null) return "Unknown";

        String path = dim.getPath().replace("_", " ");
        String[] words = path.split(" ");
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (words[i].isEmpty()) continue;
            formatted.append(Character.toUpperCase(words[i].charAt(0)))
                    .append(words[i].substring(1).toLowerCase());
            if (i < words.length - 1) {
                formatted.append(" ");
            }
        }

        return formatted.toString();
    }


    @Override
    public Identifier getUid() {
        return Identifier.fromNamespaceAndPath(Portals.MOD_ID, "portal_name_provider");
    }
}

 */