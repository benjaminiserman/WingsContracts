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
    var cooldownTime: Int = 0

    fun createItem(itemStack: ItemStack) {
        val level = level ?: return
        level.addFreshEntity(
            FakeItemEntity(
                level,
                blockPos.x.toDouble(),
                blockPos.y - 0.5,
                blockPos.z.toDouble(),
                itemStack
            )
        )
    }

    companion object {
        fun serverTick(
            level: Level, blockPos: BlockPos, blockState: BlockState,
            spigot: ContractSpigotBlockEntity
        ): Boolean {
            --spigot.cooldownTime
            if (spigot.cooldownTime > 0) {
                return false
            }
            spigot.cooldownTime = 0

            val itemToSpit = SpigotLinker.get(level).itemsToSpit.removeFirstOrNull()
            if (itemToSpit == null) {
                return false
            }

            spigot.createItem(itemToSpit)
            spigot.cooldownTime = 10

            return true
        }
    }
}