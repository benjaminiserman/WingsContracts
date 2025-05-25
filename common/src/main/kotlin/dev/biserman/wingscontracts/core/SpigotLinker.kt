package dev.biserman.wingscontracts.core

import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class SpigotLinker {
    val itemsToSpit = ArrayDeque<ItemStack>()

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
