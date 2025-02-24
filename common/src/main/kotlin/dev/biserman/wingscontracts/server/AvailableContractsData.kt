@file:OptIn(ExperimentalStdlibApi::class)

package dev.biserman.wingscontracts.server

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.api.AbyssalContract
import dev.biserman.wingscontracts.data.AvailableContractsManager
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.ContainerHelper
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData

class AvailableContractsData : SavedData() {
    val container = AvailableContractsContainer(this)
    var currentCycleStart: Long = 0

    override fun save(compoundTag: CompoundTag): CompoundTag? {
        val contractListTag = CompoundTag()
        ContainerHelper.saveAllItems(contractListTag, container.items)
        compoundTag.put(CONTRACT_LIST, contractListTag)
        compoundTag.putLong(CURRENT_CYCLE_START, currentCycleStart)
        return compoundTag
    }

    fun serverTick() {
        val cyclesPassed = (System.currentTimeMillis() - currentCycleStart) / CYCLE_DURATION_MS

        if (cyclesPassed <= 0) {
            return
        }

        currentCycleStart += cyclesPassed * CYCLE_DURATION_MS

        container.clearContent()
        for (i in 0..<container.containerSize) {
            container.items[i] = generateContract()
        }
    }

    fun generateContract(): ItemStack = AbyssalContract.load(AvailableContractsManager.availableContracts.random()).createItem()

    companion object {
        const val MAX_OPTIONS = 5
        const val CONTRACT_LIST = "contractList"
        const val CURRENT_CYCLE_START = "currentCycleStart"
        const val CYCLE_DURATION_MS = 1000L * 60 * 5
        const val IDENTIFIER = "${WingsContractsMod.MOD_ID}_world_data"

        fun load(nbt: CompoundTag): AvailableContractsData {
            val availableContracts = AvailableContractsData()
            ContainerHelper.loadAllItems(nbt.getCompound(CONTRACT_LIST), availableContracts.container.items)
            availableContracts.currentCycleStart = nbt.getLong(CURRENT_CYCLE_START)
            return availableContracts
        }

        fun get(world: Level): AvailableContractsData {
            if (world !is ServerLevel) {
                return AvailableContractsData()
            }

            val data = world.server.getLevel(Level.OVERWORLD)!!.dataStorage.computeIfAbsent(
                AvailableContractsData::load,
                ::AvailableContractsData,
                IDENTIFIER
            )

            return data
        }
    }
}