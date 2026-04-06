package com.benbenlaw.portals.client;

import com.benbenlaw.portals.Portals;
import com.benbenlaw.portals.block.PortalsBlocks;
import com.benbenlaw.portals.util.CustomPortalApiRegistry;
import com.benbenlaw.portals.util.CustomPortalHelper;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = Portals.MOD_ID, value = Dist.CLIENT)
public class CustomPortalsModClient {

    private CustomPortalsModClient() {}

    //@SubscribeEvent
    //public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
    //    event.register((state, world, pos, tintIndex) -> {
    //        if (pos != null && world instanceof RenderChunkRegion) {
    //            var block = CustomPortalHelper.getPortalBase(((ChunkRendererRegionAccessor) world).getLevel(), pos);
    //            var link = CustomPortalApiRegistry.getPortalLinkFromBase(block);
    //            if (link != null)
    //                return link.colorID;
    //        }
    //        return 1908001;
    //    }, PortalsBlocks.CUSTOM_PORTAL.get());
    //}


}