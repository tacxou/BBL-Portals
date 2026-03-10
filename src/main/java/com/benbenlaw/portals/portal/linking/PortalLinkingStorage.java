package com.benbenlaw.portals.portal.linking;

import com.benbenlaw.portals.portal.DimensionLink;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PortalLinkingStorage extends SavedData {


    public static final Codec<PortalLinkingStorage> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    DimensionLink.CODEC.listOf().fieldOf("dimensionLinks").forGetter(PortalLinkingStorage::getDimensionLinks)
            ).apply(instance, PortalLinkingStorage::new)
    );

    public static final SavedDataType<PortalLinkingStorage> TYPE = new SavedDataType<>(
            Identifier.parse("dimension_links"),
            PortalLinkingStorage::new,
            CODEC,
            null
    );

    private final List<DimensionLink> dimensionLinks = new ArrayList<>();

    public List<DimensionLink> getDimensionLinks() {
        return dimensionLinks;
    }


    public PortalLinkingStorage() {}

    public PortalLinkingStorage(List<DimensionLink> portalLinks) {
        this.dimensionLinks.addAll(portalLinks);
    }
    public GlobalPos getDestination(BlockPos portalFramePos, ResourceKey<Level> dimID) {
        for (DimensionLink link : dimensionLinks) {
            if (link.fromPos().dimension().identifier() == dimID.identifier() && link.fromPos().pos().equals(portalFramePos)) {
                return link.toPos();
            }
        }

        return null;
    }

    public void createLink(BlockPos portalFramePos, ResourceKey<Level> dimID, BlockPos destPortalFramePos, ResourceKey<Level> destDimID) {
        addLink(portalFramePos, dimID, destPortalFramePos, destDimID);
        addLink(destPortalFramePos, destDimID, portalFramePos, dimID);
    }

    private void addLink(BlockPos portalFramePos, Identifier dimID, BlockPos destPortalFramePos, Identifier destDimID ) {
        boolean found = false;
        for (DimensionLink link : dimensionLinks) {
            if (link.fromPos().dimension().identifier() == dimID && link.fromPos().pos().equals(portalFramePos)) {
                found = true;
                break;
            }
        }

        if (!found) {
            dimensionLinks.add(
                    new DimensionLink(new GlobalPos(ResourceKey.create(Registries.DIMENSION, dimID), portalFramePos),
                            new GlobalPos(ResourceKey.create(Registries.DIMENSION, destDimID), destPortalFramePos))
            );
        }
    }

    private void addLink(BlockPos portalFramePos, ResourceKey<Level> dimID, BlockPos destPortalFramePos, ResourceKey<Level> destDimID) {
        addLink(portalFramePos, dimID.identifier(), destPortalFramePos, destDimID.identifier());
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}