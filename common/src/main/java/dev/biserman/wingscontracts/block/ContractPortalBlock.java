package dev.biserman.wingscontracts.block;

import org.jetbrains.annotations.NotNull;

import dev.biserman.wingscontracts.block.state.properties.ContractPortalMode;
import dev.biserman.wingscontracts.item.ContractItem;
import dev.biserman.wingscontracts.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ContractPortalBlock extends BaseEntityBlock {
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
    public InteractionResult use(final BlockState blockState, final Level level, final BlockPos blockPos,
            final Player player, final InteractionHand interactionHand, final BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            return InteractionResult.PASS;
        }

        var blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof ContractPortalBlockEntity portalBlockEntity)) {
            return InteractionResult.FAIL;
        }

        var contractSlotItem = portalBlockEntity.getContractSlot();
        if (contractSlotItem.isEmpty()) {
            var itemInHand = player.getItemInHand(interactionHand);
            if (!(itemInHand.getItem() instanceof ContractItem)) {
                return InteractionResult.FAIL;
            }

            portalBlockEntity.setContractSlot(itemInHand);
            player.setItemInHand(interactionHand, ItemStack.EMPTY);
            level.setBlockAndUpdate(blockPos, blockState.setValue(ContractPortalBlock.MODE, ContractPortalMode.LIT));
        } else {
            portalBlockEntity.setContractSlot(ItemStack.EMPTY);
            spawnItem(contractSlotItem, level, blockPos);
            level.setBlockAndUpdate(blockPos, blockState.setValue(ContractPortalBlock.MODE, ContractPortalMode.UNLIT));
        }

        level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
        return InteractionResult.SUCCESS;

        // if (blockState.getValue(MODE) == ContractPortalMode.UNLIT) {
        // var contractTag = new CompoundTag();
        // contractTag.putString("hello", "world");
        // var contractItem = new ItemStack(ItemRegistry.CONTRACT.get());
        // contractItem.setTag(contractTag);
        // ItemEntity itemEntity = new ItemEntity(
        // level,
        // blockPos.getX(),
        // blockPos.getY() + 1,
        // blockPos.getZ(),
        // contractItem);
        // level.addFreshEntity(itemEntity);
        // }

        // final BlockState endBlockState = blockState.cycle(ContractPortalBlock.MODE);
        // level.setBlockAndUpdate(blockPos, endBlockState);
        // return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public void spawnItem(ItemStack itemStack, Level level, BlockPos blockPos) {
        ItemEntity itemEntity = new ItemEntity(level, blockPos.getX(), blockPos.getY() + 0.75, blockPos.getZ(), itemStack);
        var magnitude = 0;
        var radians = level.random.nextDouble() * Math.PI * 2;
        itemEntity.setDeltaMovement(Math.sin(radians) * magnitude, magnitude, Math.cos(radians) * magnitude);
        level.addFreshEntity(itemEntity);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos,
            CollisionContext collisionContext) {
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

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState,
            BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null
                : createTickerHelper(blockEntityType, BlockEntityRegistry.CONTRACT_PORTAL.get(),
                        ContractPortalBlockEntity::serverTick);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ContractPortalBlockEntity(blockPos, blockState);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }
}
