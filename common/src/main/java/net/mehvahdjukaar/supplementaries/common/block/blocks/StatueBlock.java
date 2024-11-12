package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.StatueBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class StatueBlock extends WaterBlock implements EntityBlock {
    protected static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 16, 12);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public StatueBlock(Properties properties) {
        super(properties.lightLevel(state -> state.getValue(LIT) ? 7 : 0));
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false)
                .setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        boolean isPowered = level.hasNeighborSignal(pos);
        if (isPowered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, isPowered), 2);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, flag).setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()))
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, POWERED, LIT);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (worldIn.getBlockEntity(pos) instanceof StatueBlockTile tile) {
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new StatueBlockTile(pPos, pState);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof ItemDisplayTile tile) {
            return tile.interactWithPlayerItem(player, hand, stack);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof ItemDisplayTile tile) {
                Containers.dropContents(world, pos, tile);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof Container tile)
            return tile.isEmpty() ? 0 : 15;
        else
            return 0;
    }

    @Override
    public void animateTick(BlockState stateIn, Level level, BlockPos pos, RandomSource rand) {
        if (stateIn.getValue(LIT)) {
            Direction direction = stateIn.getValue(FACING).getOpposite();
            double x = pos.getX() + 0.5D - 0.1875 * direction.getStepX();
            double y = pos.getY() + 9 / 16f;
            double z = pos.getZ() + 0.5D - 0.1875 * direction.getStepZ();
            addCandleParticleAndSound(level, new Vec3(x, y, z), rand);
        }
    }

    //candle code
    public static void addCandleParticleAndSound(Level level, Vec3 vec3, RandomSource random) {
        float f = random.nextFloat();
        if (f < 0.3F) {
            level.addParticle(ParticleTypes.SMOKE, vec3.x, vec3.y, vec3.z, 0.0D, 0.0D, 0.0D);
            if (f < 0.17F) {
                level.playLocalSound(vec3.x + 0.5D, vec3.y + 0.5D, vec3.z + 0.5D, SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
            }
        }
        level.addParticle(ParticleTypes.SMALL_FLAME, vec3.x, vec3.y, vec3.z, 0.0D, 0.0D, 0.0D);
    }
}
