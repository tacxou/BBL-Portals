package com.benbenlaw.portals;

import com.benbenlaw.portals.api.CustomPortalBuilder;
import com.benbenlaw.portals.block.PortalTextures;
import com.benbenlaw.portals.block.PortalsBlocks;
import com.benbenlaw.portals.portal.PortalPlacer;
import com.benbenlaw.portals.portal.frame.FlatPortalAreaHelper;
import com.benbenlaw.portals.portal.frame.VanillaPortalAreaHelper;
import com.benbenlaw.portals.portal.linking.PortalLinkingStorage;
import com.benbenlaw.portals.util.*;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
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
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Portals.MOD_ID)
public class Portals{
    public static final String MOD_ID = "portals";
    public static final Logger LOGGER = LogManager.getLogger();

    public static HashMap<Identifier, ResourceKey<Level>> DIMENSIONS = new HashMap<>();
    public static Identifier VANILLAPORTAL_FRAMETESTER = Identifier.fromNamespaceAndPath(MOD_ID, "vanillanether");
    public static Identifier FLATPORTAL_FRAMETESTER = Identifier.fromNamespaceAndPath(MOD_ID, "flat");
    public static PortalLinkingStorage PORTAL_LINKING_STORAGE;

    public Portals(final IEventBus eventBus, final ModContainer modContainer) {

        PortalsBlocks.BLOCKS.register(eventBus);
        NeoForge.EVENT_BUS.addListener(this::onServerStart);
        //NeoForge.EVENT_BUS.addListener(this::onRightClickItem);

        CustomPortalApiRegistry.registerPortalFrameTester(VANILLAPORTAL_FRAMETESTER, VanillaPortalAreaHelper::new);
        CustomPortalApiRegistry.registerPortalFrameTester(FLATPORTAL_FRAMETESTER, FlatPortalAreaHelper::new);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            eventBus.register(new PortalsColorHandler());
            eventBus.addListener(Portals::onClientSetup);
        }

        eventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

        if (!FMLEnvironment.isProduction()) {

            event.enqueueWork(() -> {
                CustomPortalBuilder.beginPortal()
                        .frameBlock(Blocks.DIAMOND_BLOCK)
                        .lightWithItem(Items.FLINT_AND_STEEL)
                        .destDimID(Level.END.identifier())
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
                        .destDimID(Level.NETHER.identifier())
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
                        .destDimID(Level.NETHER.identifier())
                        .customFrameTester(Portals.VANILLAPORTAL_FRAMETESTER)
                        .tintColor(0xFFD800)
                        .registerPortal();
            });

            event.enqueueWork(() -> {
                CustomPortalBuilder.beginPortal()
                        .frameBlock(Blocks.GOLD_BLOCK)
                        .lightWithItem(Items.FLINT_AND_STEEL)
                        .destDimID(Level.NETHER.identifier())
                        .customFrameTester(Portals.FLATPORTAL_FRAMETESTER)
                        .tintColor(0x007F7F)
                        .registerPortal();
            });

            event.enqueueWork(() -> {
                CustomPortalBuilder.beginPortal()
                        .frameBlock(Blocks.GRAVEL)
                        .lightWithItem(Items.FLINT_AND_STEEL)
                        .destDimID(Level.NETHER.identifier())
                        .customFrameTester(Portals.FLATPORTAL_FRAMETESTER)
                        .tintColor(0x007F7F)
                        .forcedSize(6, 10)
                        .registerPortal();
            });

        }

        /*
        if (ModList.get().isLoaded("kubejs")) {
            KubeJSRegister.register();
        }

        if (ModList.get().isLoaded("create")) {
            event.enqueueWork(CreateCompat::register);
        }

         */

    }

    private void onServerStart(ServerStartedEvent event) {
        for (ResourceKey<Level> registryKey : event.getServer().levelKeys()) DIMENSIONS.put(registryKey.identifier(), registryKey);

        PORTAL_LINKING_STORAGE = event.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(PortalLinkingStorage.TYPE);
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        //event.enqueueWork( () ->
        //        //ItemBlockRenderTypes.setRenderLayer(PortalsBlocks.CUSTOM_PORTAL.get(), ChunkSectionLayer.TRANSLUCENT)
        //);
    }

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

}
