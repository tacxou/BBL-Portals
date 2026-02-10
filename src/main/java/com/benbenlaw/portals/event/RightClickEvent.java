package com.benbenlaw.portals.event;

import com.benbenlaw.portals.Portals;
import com.benbenlaw.portals.portal.PortalPlacer;
import com.benbenlaw.portals.util.PortalIgnitionSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem;

@EventBusSubscriber(modid = Portals.MOD_ID)
public class RightClickEvent {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level world = event.getLevel();
        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);

        if (!world.isClientSide()) {
            Item item = stack.getItem();
            if (PortalIgnitionSource.isRegisteredIgnitionSourceWith(item)) {
                BlockHitResult blockHit = event.getHitVec();
                if (
                        PortalPlacer.attemptPortalLight(
                                world,
                                blockHit.getBlockPos().relative(blockHit.getDirection()),
                                PortalIgnitionSource.ItemUseSource(item).withPlayer(player)
                        )
                )
                    event.setCanceled(true);
            }
        }
    }

}
