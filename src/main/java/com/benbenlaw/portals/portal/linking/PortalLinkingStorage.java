package com.benbenlaw.portals.portal.linking;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

public class PortalLinkingStorage extends SavedData {

    public final ConcurrentHashMap<ResourceLocation, ConcurrentHashMap<BlockPos, GlobalPos>> PORTAL_LINKS =
        new ConcurrentHashMap<>();

    public PortalLinkingStorage() {
        super();
    }

    public static SavedData.Factory<PortalLinkingStorage> factory() {
        return new SavedData.Factory<>(PortalLinkingStorage::new, PortalLinkingStorage::read, DataFixTypes.LEVEL);
    }

    public static PortalLinkingStorage read(CompoundTag tag, HolderLookup.Provider provider) {
        PortalLinkingStorage storage = new PortalLinkingStorage();
        ListTag links = tag.getList("portalLinks", CompoundTag.TAG_COMPOUND);

        for (int i = 0; i < links.size(); i++) {
            CompoundTag link = links.getCompound(i);

            ResourceLocation fromDim = ResourceLocation.parse(link.getString("fromDimID"));
            BlockPos fromPos = BlockPos.of(link.getLong("fromPos"));

            GlobalPos to = GlobalPos.of(
                    ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(link.getString("toDimID"))),
                    BlockPos.of(link.getLong("toPos"))
            );

            storage.PORTAL_LINKS
                    .computeIfAbsent(fromDim, k -> new ConcurrentHashMap<>())
                    .put(fromPos, to);
        }

        return storage;
    }


    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        ListTag links = new ListTag();

        PORTAL_LINKS.forEach((fromDim, map) -> {
            map.forEach((fromPos, to) -> {
                CompoundTag link = new CompoundTag();

                link.putString("fromDimID", fromDim.toString());
                link.putLong("fromPos", fromPos.asLong());

                link.putString("toDimID", to.dimension().location().toString());
                link.putLong("toPos", to.pos().asLong());

                links.add(link);
            });
        });

        tag.put("portalLinks", links);
        return tag;
    }

    public GlobalPos getDestination(BlockPos portalFramePos, ResourceKey<Level> dimID) {
        ConcurrentHashMap<BlockPos, GlobalPos> map = PORTAL_LINKS.get(dimID.location());
        return map != null ? map.get(portalFramePos) : null;
    }

    public void createLink(BlockPos portalFramePos, ResourceKey<Level> dimID, BlockPos destPortalFramePos, ResourceKey<Level> destDimID) {
        addLink(portalFramePos, dimID, destPortalFramePos, destDimID);
        addLink(destPortalFramePos, destDimID, portalFramePos, dimID);
    }

    private void addLink(BlockPos portalFramePos, ResourceLocation dimID, BlockPos destPortalFramePos, ResourceLocation destDimID ) {
        PORTAL_LINKS
                .computeIfAbsent(dimID, k -> new ConcurrentHashMap<>())
                .put(
                        portalFramePos,
                        GlobalPos.of(
                                ResourceKey.create(Registries.DIMENSION, destDimID),
                                destPortalFramePos
                        )
                );
    }

    private void addLink(BlockPos portalFramePos, ResourceKey<Level> dimID, BlockPos destPortalFramePos, ResourceKey<Level> destDimID) {
        addLink(portalFramePos, dimID.location(), destPortalFramePos, destDimID.location());
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}