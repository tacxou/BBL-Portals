package com.benbenlaw.portals.block;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum PortalTextures implements StringRepresentable {
    DEFAULT("default"),
    NETHER("nether"),
    MOLTEN("molten");

    private final String name;

    PortalTextures(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}