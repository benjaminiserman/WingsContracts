package dev.biserman.wingscontracts.core.block;

import org.jetbrains.annotations.NotNull;

import dev.biserman.wingscontracts.core.block.state.properties.ContractPortalMode;
import dev.biserman.wingscontracts.core.item.ContractItem;
import dev.biserman.wingscontracts.core.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ContractPortalBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<ContractPortalMode> MODE = EnumProperty.create("mode", ContractPortalMode.class);

    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);

    public ContractPortalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
            .setValue(FACING, Direction.NORTH)
            .setValue(MODE, ContractPortalMode.UNLIT));
    }

    @Override
    public InteractionResult use(final BlockState blockState, final Level level, final BlockPos blockPos, final Player player, final InteractionHand interactionHand, final BlockHitResult blockHitResult) { 
        if (blockState.getValue(MODE) == ContractPortalMode.UNLIT) {
            var contractTag = new CompoundTag();
            contractTag.putString("hello", "world");
            var contractItem = new ItemStack(ItemRegistry.CONTRACT.get());
            contractItem.setTag(contractTag);
            ItemEntity itemEntity = new ItemEntity(
                level, 
                blockPos.getX(), 
                blockPos.getY() + 1, 
                blockPos.getZ(), 
                contractItem
            );
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
        
        final BlockState endBlockState = blockState.cycle(ContractPortalBlock.MODE);
        level.setBlockAndUpdate(blockPos, endBlockState);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return (this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()));
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

	public static int getLightLevel(BlockState state) {
		return switch (state.getValue(MODE)) {
            case UNLIT -> 0;
            case LIT -> 11;
            case COIN -> 15;
            default -> 0;
        };
	}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(MODE);
    }
}
