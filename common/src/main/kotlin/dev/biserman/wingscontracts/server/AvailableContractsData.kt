@file:OptIn(ExperimentalStdlibApi::class)

package dev.biserman.wingscontracts.server

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.api.AbyssalContract
import dev.biserman.wingscontracts.api.Contract.Companion.baseUnitsDemanded
import dev.biserman.wingscontracts.api.Contract.Companion.countPerUnit
import dev.biserman.wingscontracts.api.Contract.Companion.currentCycleStart
import dev.biserman.wingscontracts.api.Contract.Companion.startTime
import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.data.AvailableContractsManager
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import kotlin.math.max
import kotlin.math.roundToInt

class AvailableContractsData : SavedData() {
    val container = AvailableContractsContainer(this)
    val remainingPicks = mutableMapOf<String, Int>()
    var currentCycleStart: Long = 0
    val nextCycleStart get() = currentCycleStart + ModConfig.SERVER.availableContractsPoolRefreshPeriodMs.get()

    override fun save(compoundTag: CompoundTag): CompoundTag? {
        val contractListTag = CompoundTag()
        ContainerHelper.saveAllItems(contractListTag, container.items)
        val remainingPicksTag = CompoundTag()
        remainingPicks.forEach { userId, picks ->
            remainingPicksTag.putInt(userId, picks)
        }

        compoundTag.put(REMAINING_PICKS, remainingPicksTag)
        compoundTag.put(CONTRACT_LIST, contractListTag)
        compoundTag.putLong(CURRENT_CYCLE_START, currentCycleStart)
        return compoundTag
    }

    fun serverTick() {
        val cyclesPassed =
            (System.currentTimeMillis() - currentCycleStart) / ModConfig.SERVER.availableContractsPoolRefreshPeriodMs.get()

        if (cyclesPassed <= 0) {
            return
        }

        currentCycleStart += cyclesPassed * ModConfig.SERVER.availableContractsPoolRefreshPeriodMs.get()
        remainingPicks.keys.forEach { remainingPicks[it] = ModConfig.SERVER.availableContractsPoolPicks.get() }

        container.clearContent()
        for (i in 0..<container.containerSize) {
            container.items[i] = generateContract()
        }
    }

    fun generateContract(): ItemStack {
        val tag = AvailableContractsManager.random()
        tag.currentCycleStart = currentCycleStart
        tag.startTime = currentCycleStart
        tag.baseUnitsDemanded =
            max(1, ((tag.baseUnitsDemanded ?: 64).toDouble() * ModConfig.SERVER.defaultUnitsDemandedMultiplier.get()).roundToInt())
        tag.countPerUnit =
            max(1, ((tag.countPerUnit ?: 16).toDouble() * ModConfig.SERVER.defaultCountPerUnitMultiplier.get()).roundToInt())

        return AbyssalContract.load(tag).createItem()
    }

    companion object {
        const val CONTRACT_LIST = "contractList"
        const val REMAINING_PICKS = "remainingPicks"
        const val CURRENT_CYCLE_START = "currentCycleStart"
        const val IDENTIFIER = "${WingsContractsMod.MOD_ID}_world_data"

        fun load(nbt: CompoundTag): AvailableContractsData {
            val availableContracts = AvailableContractsData()
            ContainerHelper.loadAllItems(nbt.getCompound(CONTRACT_LIST), availableContracts.container.items)
            val remainingPicksTag = nbt.getCompound(REMAINING_PICKS)
            remainingPicksTag.allKeys.forEach { key ->
                availableContracts.remainingPicks[key] = remainingPicksTag.getInt(key)
            }
            availableContracts.currentCycleStart = nbt.getLong(CURRENT_CYCLE_START)
            return availableContracts
        }

        fun get(world: Level): AvailableContractsData {
            if (world !is ServerLevel) {
                return AvailableContractsData()
            }

            val data = world.server.getLevel(Level.OVERWORLD)!!.dataStorage.computeIfAbsent(
                AvailableContractsData::load, ::AvailableContractsData, IDENTIFIER
            )

            return data
        }

        fun remainingPicks(player: Player): Int {
            val remainingPicksMap = get(player.level()).remainingPicks
            val id = player.id.toString()
            return remainingPicksMap.computeIfAbsent(id) { ModConfig.SERVER.availableContractsPoolPicks.get() }
        }

        fun setRemainingPicks(player: Player, picks: Int) {
            val remainingPicksMap = get(player.level()).remainingPicks
            val id = player.id.toString()
            remainingPicksMap[id] = picks
        }
    }
}