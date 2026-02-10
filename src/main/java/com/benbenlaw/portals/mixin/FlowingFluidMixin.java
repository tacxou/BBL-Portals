package com.benbenlaw.portals.mixin;

import com.benbenlaw.portals.block.CustomPortalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowingFluid.class)
public class FlowingFluidMixin {

    @Inject(method = "canHoldFluid", at = @At("HEAD"), cancellable = true)
    private void preventCustomPortalsBeingReplaced(BlockGetter getter, BlockPos pos, BlockState state, Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof CustomPortalBlock) {
            cir.setReturnValue(false);
        }
    }

}
