package com.benbenlaw.portals.util;

import com.benbenlaw.portals.Portals;
import com.benbenlaw.portals.block.CustomPortalBlock;
import com.benbenlaw.portals.block.PortalsBlocks;
import com.benbenlaw.portals.event.PortalEvent;
import com.benbenlaw.portals.event.PortalIgniteEvent;
import com.benbenlaw.portals.event.PortalPreIgniteEvent;
import com.benbenlaw.portals.event.PortalSoundEvent;
import com.benbenlaw.portals.portal.frame.PortalFrameTester;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class PortalLink {

    public ResourceLocation block;

    public PortalIgnitionSource portalIgnitionSource = PortalIgnitionSource.FIRE;

    private Supplier<CustomPortalBlock> portalBlock = PortalsBlocks.CUSTOM_PORTAL;

    public ResourceLocation dimID;

    public ResourceLocation returnDimID = ResourceLocation.withDefaultNamespace("overworld");

    public boolean onlyIgnitableInReturnDim = false;

    public int colorID;

    public int forcedWidth, forcedHeight;

    public Integer portalSearchYBottom, portalSearchYTop;

    public Integer returnPortalSearchYBottom, returnPortalSearchYTop;

    public ResourceLocation portalFrameTester = Portals.VANILLAPORTAL_FRAMETESTER;

    private Consumer<Entity> postTPEvent;

    private final PortalEvent<Entity, ShouldTeleport> beforeTPEvent = new PortalEvent<>(ShouldTeleport.CONTINUE_TP);

    private final PortalEvent<Player, PortalSoundEvent> inPortalAmbienceEvent = new PortalEvent<>();

    private final PortalEvent<Player, PortalSoundEvent> postTpPortalAmbienceEvent = new PortalEvent<>();

    private PortalIgniteEvent portalIgniteEvent = (player, world, portalPos, framePos, portalIgnitionSource) -> {};

    private PortalPreIgniteEvent portalPreIgniteEvent = (player, world, portalPos, framePos, portalIgnitionSource) -> true;

    public PortalLink() {}

    public PortalLink(ResourceLocation blockID, ResourceLocation dimID, int colorID) {
        this.block = blockID;
        this.dimID = dimID;
        this.colorID = colorID;
    }

    public CustomPortalBlock getPortalBlock() {
        return portalBlock.get();
    }

    public void setPortalBlock(Supplier<CustomPortalBlock> block) {
        this.portalBlock = block;
    }

    public boolean doesIgnitionMatch(PortalIgnitionSource attemptedSource) {
        return portalIgnitionSource.sourceType == attemptedSource.sourceType && portalIgnitionSource.ignitionSourceID.equals(
            attemptedSource.ignitionSourceID
        );
    }

    public boolean canLightInDim(ResourceLocation dim) {
        if (!onlyIgnitableInReturnDim)
            return true;
        return dim.equals(returnDimID) || dim.equals(dimID);
    }

    public PortalEvent<Entity, ShouldTeleport> getBeforeTPEvent() {
        return beforeTPEvent;
    }

    public PortalEvent<Player, PortalSoundEvent> getInPortalAmbienceEvent() {
        return inPortalAmbienceEvent;
    }

    public PortalEvent<Player, PortalSoundEvent> getPostTpPortalAmbienceEvent() {
        return postTpPortalAmbienceEvent;
    }

    public void setPostTPEvent(Consumer<Entity> event) {
        postTPEvent = event;
    }

    public void executePostTPEvent(Entity entity) {
        if (postTPEvent != null)
            postTPEvent.accept(entity);
    }

    public PortalIgniteEvent getPortalIgniteEvent() {
        return portalIgniteEvent;
    }

    public void setPortalIgniteEvent(PortalIgniteEvent portalIgniteEvent) {
        this.portalIgniteEvent = portalIgniteEvent;
    }

    public PortalPreIgniteEvent getPortalPreIgniteEvent() {
        return portalPreIgniteEvent;
    }

    public void setPortalPreIgniteEvent(PortalPreIgniteEvent portalPreIgniteEvent) {
        this.portalPreIgniteEvent = portalPreIgniteEvent;
    }

    public PortalFrameTester.PortalFrameTesterFactory getFrameTester() {
        return CustomPortalApiRegistry.getPortalFrameTester(portalFrameTester);
    }

    public int getTintColor() {
        return colorID;
    }

    public ResourceLocation fromDimension() {
        return dimID;
    }

     public ResourceLocation toDimension() {
        return returnDimID;
    }


}