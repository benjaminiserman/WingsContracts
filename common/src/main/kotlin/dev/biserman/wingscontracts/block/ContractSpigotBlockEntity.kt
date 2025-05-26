package dev.biserman.wingscontracts.block

import dev.biserman.wingscontracts.core.SpigotLinker
import dev.biserman.wingscontracts.entity.FakeItemEntity
import dev.biserman.wingscontracts.registry.ModBlockEntityRegistry
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class ContractSpigotBlockEntity(
    blockPos: BlockPos,
    blockState: BlockState
) : BlockEntity(
    ModBlockEntityRegistry.CONTRACT_SPIGOT.get(),
    blockPos,
    blockState
) {
    fun spitItem(itemStack: ItemStack) {
        val level = level ?: return
        level.addFreshEntity(
            FakeItemEntity(
                level,
                blockPos.x + 0.5,
                blockPos.y - 0.5,
                blockPos.z + 0.5,
                itemStack
            )
        )
    }

    override fun setLevel(level: Level) {
        super.setLevel(level)
        SpigotLinker.get(level).linkedSpigots.add(this)
    }

    override fun setRemoved() {
        super.setRemoved()
        SpigotLinker.get(level ?: return).linkedSpigots.remove(this)
    }
}