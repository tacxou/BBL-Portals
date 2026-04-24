package com.benbenlaw.portals.api;

import com.benbenlaw.portals.Portals;
import com.benbenlaw.portals.block.CustomPortalBlock;
import com.benbenlaw.portals.block.PortalTextures;
import com.benbenlaw.portals.event.PortalIgniteEvent;
import com.benbenlaw.portals.event.PortalPreIgniteEvent;
import com.benbenlaw.portals.event.PortalSoundEvent;
import com.benbenlaw.portals.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CustomPortalBuilder {

    public final PortalLink portalLink;

    private CustomPortalBuilder() {
        portalLink = new PortalLink();
    }

    /**
     * Begin the creation of a new Portal
     *
     * @return an instance of CustomPortalBuilder to begin configuring the portal
     */
    public static CustomPortalBuilder beginPortal() {
        return new CustomPortalBuilder();
    }

    /**
     * Register the portal when completed. This should be called last, only when you are finished configuring the portal
     */
    public void registerPortal() {
        Block frame = BuiltInRegistries.BLOCK.get(portalLink.block);

        Portals.LOGGER.info("Registering portal:");
        Portals.LOGGER.info(" Frame block: {}", portalLink.block);
        Portals.LOGGER.info(" Frame resolved: {}", frame);
        Portals.LOGGER.info(" Dest dim: {}", portalLink.dimID);
        Portals.LOGGER.info(" Frame tester: {}", portalLink.portalFrameTester);

        CustomPortalApiRegistry.addPortal(frame, portalLink);
    }

    /**
     * Specify the Block ResourceLocation to be used as the Frame
     *
     * @param blockID Block identifier of the portal's frame block
     */
    public CustomPortalBuilder frameBlock(ResourceLocation blockID) {
        portalLink.block = blockID;
        return this;
    }

    /**
     * Specify the Block to be used as the Frame
     *
     * @param block The Block to be used as the portal's frame block
     */
    public CustomPortalBuilder frameBlock(Block block) {
        portalLink.block = BuiltInRegistries.BLOCK.getKey(block);
        return this;
    }

    /**
     * Specify the destination for the portal
     *
     * @param dimID ResourceLocation of the Dimension the portal will travel to
     */
    public CustomPortalBuilder destDimID(ResourceLocation dimID) {
        portalLink.dimID = dimID;
        return this;
    }

    /**
     * Specify the color to be used to tint the portal block.
     *
     * @param color Single Color int value used for tinting. See {@link net.minecraft.util.ColorRGBA}
     */
    public CustomPortalBuilder tintColor(int color) {
        portalLink.colorID = color;
        return this;
    }

    /**
     * Specify the color in RGB to be used to tint the portal block.
     */
    public CustomPortalBuilder tintColor(int r, int g, int b) {
        portalLink.colorID = ColorUtil.getColorFromRGB(r, g, b);
        return this;
    }

    /**
     * This portal will be ignited by water
     */
    public CustomPortalBuilder lightWithWater() {
        portalLink.portalIgnitionSource = PortalIgnitionSource.WATER;
        return this;
    }

    /**
     * This portal will be ignited by an item
     *
     * @param item Item to be used to ignite the portal
     */
    public CustomPortalBuilder lightWithItem(Item item) {
        portalLink.portalIgnitionSource = PortalIgnitionSource.ItemUseSource(item);
        return this;
    }

    /**
     * This portal will be ignited by a fluid
     *
     * @param fluid Fluid to be used to ignite the portal
     */
    public CustomPortalBuilder lightWithFluid(Fluid fluid) {
        portalLink.portalIgnitionSource = PortalIgnitionSource.FluidSource(fluid);
        return this;
    }

    /**
     * Specify a Custom Ignition Source to be used to ignite the portal. You must manually trigger the ignition
     * yourself.
     */
    public CustomPortalBuilder customIgnitionSource(ResourceLocation customSourceID) {
        portalLink.portalIgnitionSource = PortalIgnitionSource.CustomSource(customSourceID);
        return this;
    }

    /**
     * Specify a Custom Ignition Source to be used to ignite the portal. You must manually trigger the ignition
     * yourself.
     */
    public CustomPortalBuilder customIgnitionSource(PortalIgnitionSource ignitionSource) {
        portalLink.portalIgnitionSource = ignitionSource;
        return this;
    }

    /**
     * Specify the forced size of the portal Portal will only be ignitable for these exact dimensions
     *
     * @param width  Forced width of portal
     * @param height Forced height of portal
     */
    public CustomPortalBuilder forcedSize(int width, int height) {
        portalLink.forcedWidth = width;
        portalLink.forcedHeight = height;
        return this;
    }

    /**
     * Specify a custom block to be used as the portal block. Block must extend CustomPortalBlock
     */
    public CustomPortalBuilder customPortalBlock(Supplier<CustomPortalBlock> portalBlock) {
        portalLink.setPortalBlock(portalBlock);
        return this;
    }

    /**
     * Specify the dimension this portal will return you to
     *
     * @param returnDimID              Identifer of the dimmension the portal will return you to when leaving
     *                                 destination
     * @param onlyIgnitableInReturnDim Should this portal only be ignitable in returnDimID
     */
    public CustomPortalBuilder returnDim(ResourceLocation returnDimID, boolean onlyIgnitableInReturnDim) {
        portalLink.returnDimID = returnDimID;
        portalLink.onlyIgnitableInReturnDim = onlyIgnitableInReturnDim;
        return this;
    }

    /**
     * Specify that this portal can only be ignited in the Overworld Attempting to light it in other dimensions will
     * fail
     */
    public CustomPortalBuilder onlyLightInOverworld() {
        portalLink.onlyIgnitableInReturnDim = true;
        return this;
    }

    /**
     * Specify that this is a flat portal (end portal style)
     */
    public CustomPortalBuilder flatPortal() {
        portalLink.portalFrameTester = Portals.FLATPORTAL_FRAMETESTER;
        return this;
    }

    /**
     * Specify a custom portal frame tester to be used.
     */
    public CustomPortalBuilder customFrameTester(ResourceLocation frameTester) {
        portalLink.portalFrameTester = frameTester;
        return this;
    }

    /**
     * Register an event to be called immediately before the specified entity is teleported. The teleportation can be
     * cancelled by returning SHOULDTP.CANCEL_TP
     */
    public CustomPortalBuilder registerBeforeTPEvent(Function<Entity, ShouldTeleport> event) {
        portalLink.getBeforeTPEvent().register(event);
        return this;
    }

    /**
     * Register a sound to be played when the player in standing in the portal CPASoundEventData is just a stub for
     * PositionSoundAmbience as it does not exist serverside
     */
    public CustomPortalBuilder registerInPortalAmbienceSound(Function<Player, PortalSoundEvent> event) {
        portalLink.getInPortalAmbienceEvent().register(event);
        return this;
    }

    /**
     * Register a sound to be played when the player teleports CPASoundEventData is just a stub for
     * PositionSoundAmbience as it does not exist serverside
     */
    public CustomPortalBuilder registerPostTPPortalAmbience(Function<Player, PortalSoundEvent> event) {
        portalLink.getPostTpPortalAmbienceEvent().register(event);
        return this;
    }

    /**
     * Register an event to be called after the specified entity is teleported.
     */
    public CustomPortalBuilder registerPostTPEvent(Consumer<Entity> event) {
        portalLink.setPostTPEvent(event);
        return this;
    }

    /**
     * Register an event to be called before a portal is lit. PortalPreIgniteEvent returns true if the portal should be
     * lit, or false if not
     */
    public CustomPortalBuilder registerPreIgniteEvent(PortalPreIgniteEvent event) {
        portalLink.setPortalPreIgniteEvent(event);
        return this;
    }

    /**
     * Register an event to be called after a portal is lit.
     */
    public CustomPortalBuilder registerIgniteEvent(PortalIgniteEvent event) {
        portalLink.setPortalIgniteEvent(event);
        return this;
    }

    /**
     * Texture that is used for the portal, can be default, nether and molten.
     */
    public CustomPortalBuilder portalTexture(PortalTextures texture) {
        portalLink.portalTexture = texture;
        return this;
    }

    /**
     * If a particle should be shown from the portal.
     */
    public CustomPortalBuilder showParticles(boolean showParticles) {
        portalLink.showParticles = showParticles;
        return this;
    }

    /**
     * Whether the portal shows in JEI, by default all portals will show.
     */
    public CustomPortalBuilder showInJEI(boolean showInJEI) {
        portalLink.showInJEI = showInJEI;
        return this;
    }

    /**
     * Specify a Velocity (or BungeeCord) backend server name. When set, entering the portal transfers
     * the player to that server via the "bungeecord:main" plugin message channel instead of performing
     * a local dimension teleport.
     */
    public CustomPortalBuilder setServer(String serverName) {
        portalLink.targetServer = serverName;
        return this;
    }

    /**
     * Alias for {@link #destDimID(ResourceLocation)} for scripting APIs that expose "setDimension".
     * This destination is also used for inter-server transfers when {@link #setServer(String)} is set.
     */
    public CustomPortalBuilder setDimension(ResourceLocation dimension) {
        return destDimID(dimension);
    }

    /**
     * Allow (or deny) automatic portal creation on the target server during an inter-server transfer.
     * If false, arrival only reuses an existing portal (or linked location) and never creates a new one.
     */
    public CustomPortalBuilder allowInterServerPortalCreation(boolean allowCreation) {
        portalLink.allowInterServerPortalCreation = allowCreation;
        return this;
    }

    /**
     * Set a per-player cooldown (in seconds) between portal uses. While on cooldown, the player is
     * shown an action-bar message and pushed away from the portal.
     */
    public CustomPortalBuilder setCooldown(int seconds) {
        portalLink.cooldownTicks = Math.max(0, seconds) * 20;
        return this;
    }

    /**
     * Set a fixed destination position for the forward teleport (from returnDim to destDim).
     * When set, the portal ignores the paired-portal linking logic on the forward trip and
     * drops the entity at the exact coordinates given. The return trip still uses normal linking.
     */
    public CustomPortalBuilder setDestinationPos(double x, double y, double z) {
        portalLink.destinationPos = new net.minecraft.world.phys.Vec3(x, y, z);
        return this;
    }

}