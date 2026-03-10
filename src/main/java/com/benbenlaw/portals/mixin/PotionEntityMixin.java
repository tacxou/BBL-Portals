package com.benbenlaw.portals.mixin;

import com.benbenlaw.portals.portal.PortalPlacer;
import com.benbenlaw.portals.util.PortalIgnitionSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractThrownPotion.class)
public abstract class PotionEntityMixin extends ThrowableItemProjectile {

    public PotionEntityMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "dowseFire", at = @At("HEAD"))
    public void attemptPortalLight(BlockPos pos, CallbackInfo ci) {
        PortalPlacer.attemptPortalLight(this.level(), pos, PortalIgnitionSource.WATER);
    }
}