package com.benbenlaw.portals.integration.jei;

import com.benbenlaw.portals.Portals;
import com.benbenlaw.portals.block.CustomPortalBlock;
import com.benbenlaw.portals.util.PortalIgnitionSource;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PortalCategory implements IRecipeCategory<PortalRecipe> {

    public final static Identifier TEXTURE = Portals.identifier("textures/gui/jei_portal_category.png");
    static final IRecipeType<PortalRecipe> RECIPE_TYPE = IRecipeType.create(Portals.MOD_ID, "portal_category", PortalRecipe.class);

    private final int height = 100;
    private final int width = 140;
    private final IDrawable icon;

    public PortalCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.OBSIDIAN.asItem()));
    }

    @Override
    public IRecipeType<PortalRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("category.portals.portal_recipe");
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public @org.jspecify.annotations.Nullable Identifier getIdentifier(PortalRecipe recipe) {
        String fromDim = recipe.fromDimension().toString().replace(':', '_');
        String toDim = recipe.toDimension().toString().replace(':', '_');
        String frame = BuiltInRegistries.BLOCK.getKey(recipe.portalFrame().getBlock()).toString().replace(':', '_');

        return Identifier.parse(fromDim + "_" + toDim + "_" + frame);
    }


    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PortalRecipe recipe, IFocusGroup iFocusGroup) {

        PortalIgnitionSource ignitionSource = recipe.ignitionSource();
        PortalIgnitionSource.SourceType sourceType = ignitionSource.sourceType;

        if (sourceType == PortalIgnitionSource.SourceType.USEITEM) {
            ItemStack litItem = ignitionSource.getIgnetionItemStack();
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY,44 ,1).add(litItem).addRichTooltipCallback(
                    (recipeSlotView, tooltip) -> {
                        tooltip.add(Component.translatable("portals.jei.tooltip.item_ignition"));
                    }
            );
        }

        if (sourceType == PortalIgnitionSource.SourceType.FLUID) {
            ItemStack litItem = ignitionSource.getFluidIgnitionAsBucket();
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY,44 ,1).add(litItem).addRichTooltipCallback(
                    (recipeSlotView, tooltip) -> {
                        tooltip.add(Component.translatable("portals.jei.tooltip.fluid_ignition"));
                    }
            );
        }

        if (sourceType == PortalIgnitionSource.SourceType.BLOCKPLACED) {
            ItemStack blockItem = ignitionSource.getBlockPlacedAsItemStack();
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY,44 ,1).add(blockItem).addRichTooltipCallback(
                    (recipeSlotView, tooltip) -> {
                        tooltip.add(Component.translatable("portals.jei.tooltip.place_block_ignition"));
                    }
            );
        }

        builder.addSlot(RecipeIngredientRole.INPUT, 80, 1).add(recipe.portalFrame().getBlock().asItem().getDefaultInstance());

    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, PortalRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {

        double left = 21;
        double top = 20;
        double right = 115;
        double bottom = 100;

        if (mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom) {
            int height = recipe.portalHeight();
            int width = recipe.portalWidth();
            tooltip.add(Component.translatable("portals.jei.tooltip.rotate", width, height));
            tooltip.add(Component.translatable("portals.jei.tooltip.corners", width, height));

            if (Objects.equals(recipe.frameTester(), Identifier.fromNamespaceAndPath(Portals.MOD_ID, "vanillanether"))) {
                tooltip.add(Component.translatable("portals.jei.tooltip.nether"));
            }

            if (Objects.equals(recipe.frameTester(), Identifier.fromNamespaceAndPath(Portals.MOD_ID, "flat"))) {
                tooltip.add(Component.translatable("portals.jei.tooltip.flat"));
            }
        }
    }

    @Override
    public void draw(PortalRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        /*
        PoseStack poseStack = guiGraphics.pose();
        //RenderSystem.enableDepthTest();
        //Lighting.setupFor3DItems();

        poseStack.pushPose();
        poseStack.translate(70, 60, 50);

        float scale = 70f / Math.max(recipe.portalHeight(), recipe.portalWidth());
        poseStack.scale(scale, -scale, scale);

        float angle = (System.currentTimeMillis() % 10000L) / 10000.0F * 360.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        if (Objects.equals(recipe.frameTester(), Identifier.fromNamespaceAndPath(Portals.MOD_ID, "vanillanether"))) {
            BlockState frame = recipe.portalFrame();
            BlockState portal = recipe.portalBlock();
            renderNetherPortalLike(frame, portal, recipe.portalWidth(), recipe.portalHeight(), poseStack);
        }

        if (Objects.equals(recipe.frameTester(), Identifier.fromNamespaceAndPath(Portals.MOD_ID, "flat"))) {
            BlockState frame = recipe.portalFrame();
            BlockState portal = recipe.portalBlock().setValue(CustomPortalBlock.AXIS, Direction.Axis.Y);

            poseStack.mulPose(Axis.XP.rotationDegrees(40));

            renderEndPortalLike(frame, portal, recipe.portalWidth(), recipe.portalHeight(), poseStack);
        }

        poseStack.popPose();

        //Lighting.setupForFlatItems();
        //RenderSystem.disableDepthTest();

         */

    }

    /*
    private void renderNetherPortalLike(BlockState frame, BlockState portal, int width, int height, PoseStack poseStack) {

        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                boolean isFrame = x == 0 || x == width - 1 || y == 0 || y == height - 1;

                BlockState state = isFrame ? frame : portal;

                poseStack.pushPose();
                poseStack.translate(x - width / 2.0, y - height / 2.0, 0);

                dispatcher.renderSingleBlock(
                        state,
                        poseStack,
                        buffer,
                        0xF000F0,
                        OverlayTexture.NO_OVERLAY,
                        mc.level,
                        BlockPos.ZERO,
                        layer -> layer == ChunkSectionLayer.TRANSLUCENT
                );

                poseStack.popPose();
            }
        }

        buffer.endBatch();



    }

    private void renderEndPortalLike(BlockState frame, BlockState portal, int width, int length, PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {

                boolean isFrame =
                        x == 0 || x == width - 1 ||
                                z == 0 || z == length - 1;

                BlockState state = isFrame ? frame : portal;

                poseStack.pushPose();
                poseStack.translate(
                        x - width / 2.0,
                        0,
                        z - length / 2.0
                );

                dispatcher.renderSingleBlock(
                        state,
                        poseStack,
                        buffer,
                        0xF000F0,
                        OverlayTexture.NO_OVERLAY,
                        mc.level,
                        BlockPos.ZERO,
                        layer -> layer == ChunkSectionLayer.TRANSLUCENT
                );
                poseStack.popPose();
            }
        }

        buffer.endBatch();
    }

     */
}
