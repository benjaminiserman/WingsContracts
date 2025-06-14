package dev.biserman.wingscontracts.block

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
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

    override fun codec() = CODEC

    override fun getRenderShape(blockState: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    companion object {
        val CODEC: MapCodec<ContractSpigotBlock> = simpleCodec { properties -> ContractSpigotBlock(properties) }
        private val SHAPE: VoxelShape = box(0.0, 4.0, 0.0, 16.0, 16.0, 16.0)
    }
}