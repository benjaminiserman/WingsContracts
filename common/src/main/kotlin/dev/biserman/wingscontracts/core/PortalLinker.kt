package dev.biserman.wingscontracts.core

import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import net.minecraft.world.level.Level
import java.util.*

class PortalLinker {
    val linkedPortals = mutableMapOf<UUID, ContractPortalBlockEntity>()

    companion object {
        private val linkers = mutableMapOf<Level?, PortalLinker>()
        fun get(level: Level): PortalLinker {
            val overworld = level.server?.getLevel(Level.OVERWORLD)
            val foundLinker = linkers[overworld]
            if (foundLinker != null) {
                return foundLinker
            } else {
                val linker = PortalLinker()
                linkers[overworld] = linker
                return linker
            }
        }
    }
}