@file:Suppress("OVERRIDE_DEPRECATION")

package dev.biserman.wingscontracts.block

import dev.biserman.wingscontracts.block.state.properties.ContractPortalMode
import dev.biserman.wingscontracts.item.ContractItem
import dev.biserman.wingscontracts.registry.BlockEntityRegistry
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.Containers
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import kotlin.math.cos
import kotlin.math.sin

class ContractPortalBlock(properties: Properties) : BaseEntityBlock(properties) {
    init {
        this.registerDefaultState(
            defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(MODE, ContractPortalMode.UNLIT)
        )
    }

    override fun use(
        blockState: BlockState, level: Level, blockPos: BlockPos,
        player: Player, interactionHand: InteractionHand, blockHitResult: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS
        }

        val blockEntity = level.getBlockEntity(blockPos) as? ContractPortalBlockEntity
            ?: return InteractionResult.FAIL

        val contractSlotItem = blockEntity.contractSlot
        if (contractSlotItem.isEmpty) {
            val itemInHand = player.getItemInHand(interactionHand)
            if (itemInHand.item !is ContractItem) {
                return InteractionResult.FAIL
            }

            player.setItemInHand(interactionHand, ItemStack.EMPTY)
            blockEntity.contractSlot = itemInHand
            level.setBlockAndUpdate(blockPos, blockState.setValue(MODE, ContractPortalMode.LIT))
        } else {
            val contract = blockEntity.contractSlot
            blockEntity.contractSlot = ItemStack.EMPTY
            player.setItemInHand(interactionHand, contract)
//            spawnItem(contractSlotItem, level, blockPos)
            level.setBlockAndUpdate(blockPos, blockState.setValue(MODE, ContractPortalMode.UNLIT))
        }

        level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos)
        level.sendBlockUpdated(blockPos, blockState, blockState, UPDATE_ALL)
        return InteractionResult.SUCCESS
    }

    private fun spawnItem(itemStack: ItemStack, level: Level, blockPos: BlockPos) {
        val itemEntity = ItemEntity(level, blockPos.x + 0.5, blockPos.y + 0.75, blockPos.z + 0.5, itemStack)
        val magnitude = 0.15
        val radians = level.random.nextDouble() * Math.PI * 2
        itemEntity.setDeltaMovement(sin(radians) * magnitude, magnitude * 3, cos(radians) * magnitude)
        level.addFreshEntity(itemEntity)
    }

    override fun getShape(
        blockState: BlockState, blockGetter: BlockGetter, blockPos: BlockPos,
        collisionContext: CollisionContext
    ): VoxelShape {
        return SHAPE
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        return (defaultBlockState().setValue(FACING, context.horizontalDirection.opposite))
    }

    override fun rotate(state: BlockState, rotation: Rotation): BlockState {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)))
    }

    override fun mirror(state: BlockState, mirror: Mirror): BlockState {
        return state.rotate(mirror.getRotation(state.getValue(FACING)))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
        builder.add(MODE)
    }

    override fun <T : BlockEntity?> getTicker(
        level: Level, blockState: BlockState,
        blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return if (level.isClientSide) {
            null
        } else {
            createTickerHelper(
                blockEntityType, BlockEntityRegistry.CONTRACT_PORTAL.get(), ContractPortalBlockEntity::serverTick
            )
        }
    }

    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return ContractPortalBlockEntity(blockPos, blockState)
    }

    override fun getRenderShape(blockState: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun onRemove(
        blockState: BlockState,
        level: Level,
        blockPos: BlockPos,
        blockState2: BlockState,
        bl: Boolean
    ) {
        if (!blockState.`is`(blockState2.block)) {
            val blockEntity = level.getBlockEntity(blockPos)
            if (blockEntity is ContractPortalBlockEntity) {
                Containers.dropContents(level, blockPos, blockEntity.cachedInput)
                Containers.dropItemStack(
                    level,
                    blockPos.x.toDouble(),
                    blockPos.y.toDouble(),
                    blockPos.z.toDouble(),
                    blockEntity.contractSlot
                )
                Containers.dropItemStack(
                    level,
                    blockPos.x.toDouble(),
                    blockPos.y.toDouble(),
                    blockPos.z.toDouble(),
                    blockEntity.cachedRewards
                )
                level.updateNeighbourForOutputSignal(blockPos, this)
            }
        }
    }

    companion object {
        val FACING: DirectionProperty = HorizontalDirectionalBlock.FACING
        val MODE: EnumProperty<ContractPortalMode> = EnumProperty.create(
            "mode",
            ContractPortalMode::class.java
        )

        private val SHAPE: VoxelShape = box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0)

        fun getLightLevel(state: BlockState): Int {
            return when (state.getValue(MODE)) {
                ContractPortalMode.UNLIT -> 0
                ContractPortalMode.LIT -> 11
                ContractPortalMode.COIN -> 15
                else -> 0
            }
        }
    }
}
