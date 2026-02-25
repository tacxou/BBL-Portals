package com.benbenlaw.portals.integration.create;

import com.benbenlaw.portals.block.PortalsBlocks;
import com.simibubi.create.api.contraption.train.PortalTrackProvider;

public class CreateCompat {

    public static void register() {
        PortalTrackProvider.REGISTRY.register(
            PortalsBlocks.CUSTOM_PORTAL.get(),
            new CustomPortalTrackProvider()
        );
    }
}