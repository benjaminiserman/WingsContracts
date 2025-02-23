package dev.biserman.wingscontracts.server

import dev.biserman.wingscontracts.WingsContractsMod
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.ContainerHelper
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData

class AvailableContractsData : SavedData() {
    val container = AvailableContractsContainer()
    var currentCycleStart: Long = 0

    override fun save(compoundTag: CompoundTag): CompoundTag? {
        val contractListTag = CompoundTag()
        ContainerHelper.saveAllItems(contractListTag, container.items)
        compoundTag.put(CONTRACT_LIST, contractListTag)
        compoundTag.putLong(CURRENT_CYCLE_START, currentCycleStart)
        return compoundTag
    }

    companion object {
        const val MAX_OPTIONS = 5
        const val CONTRACT_LIST = "contractList"
        const val CURRENT_CYCLE_START = "currentCycleStart"
        const val IDENTIFIER = "${WingsContractsMod.MOD_ID}_world_data"

        fun load(nbt: CompoundTag): AvailableContractsData {
            val availableContracts = AvailableContractsData()
            ContainerHelper.loadAllItems(nbt.getCompound(CONTRACT_LIST), availableContracts.container.items)
            availableContracts.currentCycleStart = nbt.getLong(CURRENT_CYCLE_START)
            return availableContracts
        }

        fun get(world: Level): AvailableContractsData? {
            if (world !is ServerLevel) {
                return null
            }

            val data = world.server.getLevel(Level.OVERWORLD)?.dataStorage?.computeIfAbsent(
                AvailableContractsData::load,
                ::AvailableContractsData,
                IDENTIFIER
            )

            return data
        }
    }
}