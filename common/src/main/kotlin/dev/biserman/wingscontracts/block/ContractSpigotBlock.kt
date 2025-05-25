package dev.biserman.wingscontracts.block

import dev.biserman.wingscontracts.registry.ModBlockEntityRegistry
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class ContractSpigotBlock(properties: Properties) : BaseEntityBlock(properties) {
    @Deprecated("Deprecated in Java")
    override fun getShape(
        blockState: BlockState,
        blockGetter: BlockGetter,
        blockPos: BlockPos,
        collisionContext: CollisionContext
    ): VoxelShape = SHAPE

    override fun newBlockEntity(
        blockPos: BlockPos,
        blockState: BlockState
    ): BlockEntity = ContractSpigotBlockEntity(blockPos, blockState)

    override fun <T : BlockEntity?> getTicker(
        level: Level, blockState: BlockState,
        blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return if (level.isClientSide) {
            null
        } else {
            createTickerHelper(
                blockEntityType, ModBlockEntityRegistry.CONTRACT_SPIGOT.get(), ContractSpigotBlockEntity::serverTick
            )
        }
    }

    companion object {
        private val SHAPE: VoxelShape = box(0.0, 4.0, 0.0, 16.0, 16.0, 16.0)
    }
}