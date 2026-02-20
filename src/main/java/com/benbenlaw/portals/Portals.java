package com.benbenlaw.portals;

import com.benbenlaw.portals.api.CustomPortalBuilder;
import com.benbenlaw.portals.block.PortalTextures;
import com.benbenlaw.portals.block.PortalsBlocks;
import com.benbenlaw.portals.integration.kubejs.PortalBuilder;
import com.benbenlaw.portals.integration.kubejs.PortalEvents;
import com.benbenlaw.portals.portal.PortalPlacer;
import com.benbenlaw.portals.portal.frame.FlatPortalAreaHelper;
import com.benbenlaw.portals.portal.frame.VanillaPortalAreaHelper;
import com.benbenlaw.portals.portal.linking.PortalLinkingStorage;
import com.benbenlaw.portals.util.CustomPortalApiRegistry;
import com.benbenlaw.portals.util.PortalIgnitionSource;
import com.benbenlaw.portals.util.PortalLink;
import com.benbenlaw.portals.util.PortalsColorHandler;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import snownee.jade.JadeClient;
import snownee.jade.api.IWailaClientRegistration;

import java.util.HashMap;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Portals.MOD_ID)
public class Portals{
    public static final String MOD_ID = "portals";
    public static final Logger LOGGER = LogManager.getLogger();

    public static HashMap<ResourceLocation, ResourceKey<Level>> DIMENSIONS = new HashMap<>();
    public static ResourceLocation VANILLAPORTAL_FRAMETESTER = ResourceLocation.fromNamespaceAndPath(MOD_ID, "vanillanether");
    public static ResourceLocation FLATPORTAL_FRAMETESTER = ResourceLocation.fromNamespaceAndPath(MOD_ID, "flat");
    public static PortalLinkingStorage PORTAL_LINKING_STORAGE;

    public Portals(final IEventBus eventBus, final ModContainer modContainer) {

        PortalsBlocks.BLOCKS.register(eventBus);
        NeoForge.EVENT_BUS.addListener(this::onServerStart);
        //NeoForge.EVENT_BUS.addListener(this::onRightClickItem);

        CustomPortalApiRegistry.registerPortalFrameTester(VANILLAPORTAL_FRAMETESTER, VanillaPortalAreaHelper::new);
        CustomPortalApiRegistry.registerPortalFrameTester(FLATPORTAL_FRAMETESTER, FlatPortalAreaHelper::new);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            eventBus.register(new PortalsColorHandler());
        }

        eventBus.addListener(this::commonSetup);

    }

    private void commonSetup(FMLCommonSetupEvent event) {

        if (!FMLEnvironment.production) {

            event.enqueueWork(() -> {
                CustomPortalBuilder.beginPortal()
                        .frameBlock(Blocks.DIAMOND_BLOCK)
                        .lightWithItem(Items.FLINT_AND_STEEL)
                        .destDimID(Level.END.location())
                        .customFrameTester(Portals.VANILLAPORTAL_FRAMETESTER)
                        .tintColor(45, 65, 101)
                        .portalTexture(PortalTextures.MOLTEN)
                        .showInJEI(false)
                        .registerPortal();
            });


            event.enqueueWork(() -> {
                CustomPortalBuilder.beginPortal()
                        .frameBlock(Blocks.EMERALD_BLOCK)
                        .lightWithFluid(Fluids.WATER)
                        .destDimID(Level.NETHER.location())
                        .customFrameTester(Portals.VANILLAPORTAL_FRAMETESTER)
                        .showParticles(false)
                        .tintColor(0x00FF50)
                        .forcedSize(5, 5)
                        .registerPortal();
            });

            event.enqueueWork(() -> {
                CustomPortalBuilder.beginPortal()
                        .frameBlock(Blocks.IRON_BLOCK)
                        .lightWithItem(Items.FLINT_AND_STEEL)
                        .destDimID(Level.NETHER.location())
                        .customFrameTester(Portals.VANILLAPORTAL_FRAMETESTER)
                        .tintColor(0xFFD800)
                        .registerPortal();
            });

            event.enqueueWork(() -> {
                CustomPortalBuilder.beginPortal()
                        .frameBlock(Blocks.GOLD_BLOCK)
                        .lightWithItem(Items.FLINT_AND_STEEL)
                        .destDimID(Level.NETHER.location())
                        .customFrameTester(Portals.FLATPORTAL_FRAMETESTER)
                        .tintColor(0x007F7F)
                        .registerPortal();
            });

            event.enqueueWork(() -> {
                CustomPortalBuilder.beginPortal()
                        .frameBlock(Blocks.GRAVEL)
                        .lightWithItem(Items.FLINT_AND_STEEL)
                        .destDimID(Level.NETHER.location())
                        .customFrameTester(Portals.FLATPORTAL_FRAMETESTER)
                        .tintColor(0x007F7F)
                        .forcedSize(6, 10)
                        .registerPortal();
            });

        }


        if (PortalEvents.REGISTER_PORTAL.hasListeners()) {

            PortalEvents.REGISTER_PORTAL.post(new PortalBuilder());
            for (PortalBuilder.PortalMaker maker : PortalBuilder.createdPortals) {
                // Portal API 1.2.1 changes. Need to tell the user these errors
                try {
                    maker.register();
                } catch (RuntimeException e) {
                    PortalLink link = CustomPortalBuilder.beginPortal().portalLink;
                    if (link.block == null) {
                        ConsoleJS.STARTUP.error("Error registering portal for an unset frame");

                        // Clear the portal map and return because the block is null, and you can't .toString() it
                        PortalBuilder.createdPortals.clear();
                        return;
                    } else if (CustomPortalApiRegistry.getPortals().containsKey(BuiltInRegistries.BLOCK.get(link.block))) {
                        ConsoleJS.STARTUP.error("A portal of the frame '" + link.block + "' is already registered");
                    }
                    // This should never be reached, but just in case...
                    if (link.getPortalBlock() == null) {
                        ConsoleJS.STARTUP.error("[REPORT TO PORTALJS] Portal block is null for portal frame: " + link.block);
                    }
                    if (link.portalIgnitionSource == null) {
                        ConsoleJS.STARTUP.error("Custom ignition source is unset for portal frame: " + link.block);
                    }
                    if (link.dimID == null) {
                        ConsoleJS.STARTUP.error("Destination dimension is unset for portal frame: " + link.block);
                    }
                    if (!DIMENSIONS.isEmpty() && !DIMENSIONS.containsKey(link.dimID)) {
                        ConsoleJS.STARTUP.error("Dimension was not found: " + link.dimID);
                    }
                    if (PortalsBlocks.CUSTOM_PORTAL.get() == null) {
                        ConsoleJS.STARTUP.error("[REPORT TO PORTAL API] Built in CustomPortalBlock is unset");
                    }
                    if (link.block.toString().equals("minecraft:obsidian")) {
                        // The API won't approve obsidian at all
                        ConsoleJS.STARTUP.error("You can't create a portal with an obsidian base");
                    }
                }
            }
            // Needed for one time use, and to save memory
            PortalBuilder.createdPortals.clear();
        }
    }

    private void onServerStart(ServerStartedEvent event) {
        for (ResourceKey<Level> registryKey : event.getServer().levelKeys()) DIMENSIONS.put(registryKey.location(), registryKey);

        PORTAL_LINKING_STORAGE = event.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(PortalLinkingStorage.factory(), MOD_ID);
    }



}
