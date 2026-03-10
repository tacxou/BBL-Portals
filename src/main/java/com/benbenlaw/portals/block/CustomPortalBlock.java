package com.benbenlaw.portals.block;

import com.benbenlaw.portals.portal.frame.PortalFrameTester;
import com.benbenlaw.portals.util.CustomPortalApiRegistry;
import com.benbenlaw.portals.util.CustomPortalHelper;
import com.benbenlaw.portals.util.CustomTeleporter;
import com.benbenlaw.portals.util.PortalLink;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;

public class CustomPortalBlock extends Block implements Portal {

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final EnumProperty<PortalTextures> PORTAL_TEXTURES = EnumProperty.create("texture", PortalTextures.class);

    protected static final VoxelShape X_SHAPE = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    protected static final VoxelShape Z_SHAPE = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);
    protected static final VoxelShape Y_SHAPE = Block.box(0.0D, 6.0D, 0.0D, 16.0D, 10.0D, 16.0D);

    public CustomPortalBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X).setValue(PORTAL_TEXTURES, PortalTextures.DEFAULT));
    }

    @Override
    public @NotNull VoxelShape getShape(
        BlockState state,
        @NotNull BlockGetter world,
        @NotNull BlockPos pos,
        @NotNull CollisionContext context
    ) {
        return switch (state.getValue(AXIS)) {
            case Z -> Z_SHAPE;
            case Y -> Y_SHAPE;
            default -> X_SHAPE;
        };
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        return ItemStack.EMPTY;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        Block block = getPortalBase((Level) level, pos);
        PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(block);
        if (link != null) {
            PortalFrameTester portalFrameTester = link.getFrameTester()
                    .createInstanceOfPortalFrameTester()
                    .init(
                            (LevelAccessor) level,
                            pos,
                            CustomPortalHelper.getAxisFrom(state),
                            block
                    );
            if (portalFrameTester.isAlreadyLitPortalFrame())
                return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, PORTAL_TEXTURES);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos pos, RandomSource source) {
        int tintColor = CustomPortalHelper.getPortalTintColor(level, pos);
        float r = ((tintColor >> 16) & 0xFF) / 255f;
        float g = ((tintColor >> 8) & 0xFF) / 255f;
        float b = (tintColor & 0xFF) / 255f;

        if (source.nextInt(100) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    SoundEvents.PORTAL_AMBIENT,
                    SoundSource.BLOCKS,
                    0.5F,
                    source.nextFloat() * 0.4F + 0.8F,
                    false
            );
        }

        // Spawn particles

        boolean spawnPortalParticles = CustomPortalHelper.showParticles(level, pos);

        if (spawnPortalParticles) {
            for (int i = 0; i < 4; i++) {
                double x = pos.getX() + source.nextDouble();
                double y = pos.getY() + source.nextDouble();
                double z = pos.getZ() + source.nextDouble();
                double dx = (source.nextFloat() - 0.5) * 0.5;
                double dy = (source.nextFloat() - 0.5) * 0.5;
                double dz = (source.nextFloat() - 0.5) * 0.5;
                int j = source.nextInt(2) * 2 - 1;

                if (!level.getBlockState(pos.west()).is(this) && !level.getBlockState(pos.east()).is(this)) {
                    x = pos.getX() + 0.5 + 0.25 * j;
                    dx = source.nextFloat() * 2.0F * j;
                } else {
                    z = pos.getZ() + 0.5 + 0.25 * j;
                    dz = source.nextFloat() * 2.0F * j;
                }

                int colorInt = ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | ((int)(b * 255));
                level.addParticle(new DustParticleOptions(colorInt, 1.0F), x, y, z, dx, dy, dz);
            }
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }


    @Override
    public int getPortalTransitionTime(@NotNull ServerLevel world, @NotNull Entity entity) {
        if (entity instanceof Player playerEntity) {
            return Math.max(
                1,
                world.getGameRules()
                    .get(
                        playerEntity.getAbilities().invulnerable
                            ? GameRules.PLAYERS_NETHER_PORTAL_DEFAULT_DELAY
                            : GameRules.PLAYERS_NETHER_PORTAL_CREATIVE_DELAY
                    )
            );
        } else {
            return 0;
        }
    }

    public Block getPortalBase(Level world, BlockPos pos) {
        return CustomPortalHelper.getPortalBaseDefault(world, pos);
    }

    @Override
    public @Nullable TeleportTransition getPortalDestination(@NotNull ServerLevel world, @NotNull Entity entity, @NotNull BlockPos pos) {
        return CustomTeleporter.createTeleportTarget(world, entity, getPortalBase(world, pos), pos);
    }

    @Override
    public @NotNull Transition getLocalTransition() {
        return Transition.CONFUSION;
    }

}