package dev.biserman.wingscontracts.core

import dev.biserman.wingscontracts.block.ContractSpigotBlockEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import kotlin.random.Random

class SpigotLinker {
    val linkedSpigots = mutableListOf<ContractSpigotBlockEntity>()

    fun spitItem(itemStack: ItemStack) {
        linkedSpigots[Random.nextInt(linkedSpigots.count())].spitItem(itemStack)
    }

    fun spitItems(itemStacks: List<ItemStack>) {
        for (itemStack in itemStacks) {
            spitItem(itemStack)
        }
    }

    companion object {
        private val linkers = mutableMapOf<Level?, SpigotLinker>()
        fun get(level: Level): SpigotLinker {
            val overworld = level.server?.getLevel(Level.OVERWORLD)
            val foundLinker = linkers[overworld]
            if (foundLinker != null) {
                return foundLinker
            } else {
                val linker = SpigotLinker()
                linkers[overworld] = linker
                return linker
            }
        }
    }
}
