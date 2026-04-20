package com.benbenlaw.portals.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import org.jetbrains.annotations.NotNull;

public class KubeJSPortalsPlugin implements KubeJSPlugin {

    @Override
    public void registerEvents(@NotNull EventGroupRegistry registry) {
        registry.register(PortalEvents.GROUP);
    }

    @Override
    public void registerBindings(@NotNull BindingRegistry bindings) {
        bindings.add("Portals", PortalsJSBindings.class);
    }
}