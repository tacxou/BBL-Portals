package com.benbenlaw.portals.integration.jei;

import com.benbenlaw.portals.Portals;
import com.benbenlaw.portals.portal.frame.PortalFrameTester;
import com.benbenlaw.portals.util.CustomPortalApiRegistry;
import com.benbenlaw.portals.util.PortalLink;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JeiPlugin
public class JEIPortalsPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Portals.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(Blocks.OBSIDIAN.asItem().getDefaultInstance(), PortalCategory.RECIPE_TYPE);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        registration.addRecipeCategories(new PortalCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<PortalRecipe> recipes = new ArrayList<>();

        for (PortalLink link : CustomPortalApiRegistry.getAllPortalLinks()) {

            if (!link.showInJEI) continue;

            int width = link.forcedWidth != 0 ? link.forcedWidth  + 2 : 4;
            int height = link.forcedHeight != 0 ? link.forcedHeight  + 2 : 5;

            if (Objects.equals(link.portalFrameTester, ResourceLocation.fromNamespaceAndPath(Portals.MOD_ID, "flat"))) {
                width = link.forcedWidth != 0 ? link.forcedWidth + 2 : 5;
                height = link.forcedHeight != 0 ? link.forcedHeight + 2 : 5;
            }

            recipes.add(new PortalRecipe(
                    link.fromDimension(),
                    link.toDimension(),
                    width,
                    height,
                    BuiltInRegistries.BLOCK.get(link.block).defaultBlockState(),
                    link.portalBlock.get().defaultBlockState(),
                    link.portalFrameTester,
                    link.portalIgnitionSource
            ));
        }
        registration.addRecipes(PortalCategory.RECIPE_TYPE, recipes);

    }
}
