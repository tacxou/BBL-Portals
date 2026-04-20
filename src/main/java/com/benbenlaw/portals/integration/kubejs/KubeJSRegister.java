package com.benbenlaw.portals.integration.kubejs;

import com.benbenlaw.portals.api.CustomPortalBuilder;
import com.benbenlaw.portals.block.PortalsBlocks;
import com.benbenlaw.portals.util.CustomPortalApiRegistry;
import com.benbenlaw.portals.util.PortalLink;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import net.minecraft.core.registries.BuiltInRegistries;

import static com.benbenlaw.portals.Portals.DIMENSIONS;

public class KubeJSRegister {

    public static void register() {

        if (PortalEvents.REGISTER_PORTAL.hasListeners()) {

            PortalEvents.REGISTER_PORTAL.post(new PortalBuilder());
            for (PortalBuilder.PortalMaker maker : PortalBuilder.createdPortals) {
                // Portal API 1.2.1 changes. Need to tell the user these errors
                try {
                    maker.register();
                } catch (RuntimeException e) {
                    PortalLink link = maker.builder.portalLink;
                    ConsoleJS.STARTUP.error("Error registering portal: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                    if (link.block == null) {
                        ConsoleJS.STARTUP.error("Error registering portal for an unset frame (missing .frameBlock(...) call)");

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
            PortalBuilder.createdPortals.clear();
        }
    }


}
