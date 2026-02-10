package com.benbenlaw.portals.mixin.client;

import com.benbenlaw.portals.block.CustomPortalBlock;
import com.benbenlaw.portals.block.PortalsBlocks;
import com.benbenlaw.portals.mixin.PortalManagerAccessor;
import com.benbenlaw.portals.util.CustomPortalApiRegistry;
import com.benbenlaw.portals.util.PortalLink;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@OnlyIn(Dist.CLIENT)
@Mixin(Gui.class)
public class InGameHudMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private int lastColor = -1;

    @Redirect(
            method = "renderPortalOverlay",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;setColor(FFFF)V", ordinal = 0)
    )
    public void changeColor(GuiGraphics instance, float red, float green, float blue, float alpha) {
        assert minecraft.player != null;
        isCustomPortal(minecraft.player);
        if (lastColor >= 0) {
            float r = ((lastColor >> 16) & 0xFF) / 255f;
            float g = ((lastColor >> 8) & 0xFF) / 255f;
            float b = (lastColor & 0xFF) / 255f;
            RenderSystem.setShaderColor(r, g, b, alpha);
        } else {
            RenderSystem.setShaderColor(red, green, blue, alpha);
        }
    }

    @Redirect(
        method = "renderPortalOverlay", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/BlockModelShaper;getParticleIcon(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"
        )
    )
    public TextureAtlasSprite renderCustomPortalOverlay(BlockModelShaper blockModels, BlockState blockState) {
        if (lastColor >= 0) {
            return this.minecraft.getBlockRenderer()
                .getBlockModelShaper()
                .getParticleIcon(
                        PortalsBlocks.CUSTOM_PORTAL.get().defaultBlockState()
                );
        }
        return this.minecraft.getBlockRenderer()
            .getBlockModelShaper()
            .getParticleIcon(
                Blocks.NETHER_PORTAL.defaultBlockState()
            );
    }

    @Unique
    private void isCustomPortal(LocalPlayer player) {
        PortalProcessor portalManager = player.portalProcess;
        Portal portalBlock = portalManager != null && portalManager.isInsidePortalThisTick()
            ? ((PortalManagerAccessor) portalManager).getPortal()
            : null;
        BlockPos portalPos = portalManager != null && portalManager.isInsidePortalThisTick()
            ? ((PortalManagerAccessor) portalManager).getEntryPosition()
            : null;

        if (portalBlock == null) {
            return;
        }

        if (portalBlock instanceof CustomPortalBlock customportalblock) {
            PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(customportalblock.getPortalBase(player.clientLevel, portalPos));
            if (link != null) {
                lastColor = link.getTintColor();
                return;
            }
        }

        lastColor = -1;
    }
}