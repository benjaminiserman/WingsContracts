@file:OptIn(ExperimentalStdlibApi::class)

package dev.biserman.wingscontracts.server

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.config.DenominatedCurrenciesHandler
import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.core.AbyssalContract
import dev.biserman.wingscontracts.core.AbyssalContract.Companion.baseUnitsDemanded
import dev.biserman.wingscontracts.core.AbyssalContract.Companion.currentCycleStart
import dev.biserman.wingscontracts.core.AbyssalContract.Companion.reward
import dev.biserman.wingscontracts.core.Contract
import dev.biserman.wingscontracts.core.Contract.Companion.countPerUnit
import dev.biserman.wingscontracts.core.Contract.Companion.rarity
import dev.biserman.wingscontracts.core.Contract.Companion.startTime
import dev.biserman.wingscontracts.core.Contract.Companion.targetConditions
import dev.biserman.wingscontracts.core.Contract.Companion.targetItems
import dev.biserman.wingscontracts.data.AvailableContractsManager
import dev.biserman.wingscontracts.data.AvailableContractsManager.defaultRewardBagWeightSum
import dev.biserman.wingscontracts.data.AvailableContractsManager.defaultRewards
import dev.biserman.wingscontracts.data.RewardBagEntry
import dev.biserman.wingscontracts.nbt.ContractTag
import dev.biserman.wingscontracts.nbt.Reward
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import java.util.*
import kotlin.math.*

class AvailableContractsData : SavedData() {
    val container = AvailableContractsContainer()
    val remainingPicks = mutableMapOf<String, Int>()
    var currentCycleStart: Long = 0
    val nextCycleStart get() = currentCycleStart + ModConfig.SERVER.availableContractsPoolRefreshPeriodMs.get()

    val rarityThresholds by lazy { ModConfig.SERVER.rarityThresholdsString.get().split(",").map { it.toInt() } }
    val currencyHandler by lazy { DenominatedCurrenciesHandler() }

    override fun save(compoundTag: CompoundTag): CompoundTag {
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

    fun serverTick(level: ServerLevel) {
        val cyclesPassed =
            (System.currentTimeMillis() - currentCycleStart) / ModConfig.SERVER.availableContractsPoolRefreshPeriodMs.get()

        if (cyclesPassed <= 0) {
            return
        }

        WingsContractsMod.LOGGER.info("starting new cycle $currentCycleStart")
        currentCycleStart += cyclesPassed * ModConfig.SERVER.availableContractsPoolRefreshPeriodMs.get()
        remainingPicks.keys.forEach {
            remainingPicks[it] = min(
                remainingPicks[it]!! + ModConfig.SERVER.availableContractsPoolPicks.get(),
                ModConfig.SERVER.availableContractsPoolPicksCap.get()
            )
        }

        refresh(level)
    }

    fun clear(level: ServerLevel) {
        container.clearContent()
        SyncAvailableContractsMessage(level).sendToAll(level.server)
    }

    fun refresh(level: ServerLevel) {
        container.clearContent()
        for (i in 0..<container.containerSize) {
            container.items[i] = generateContract(AvailableContractsManager.randomTag()).createItem()
        }
        SyncAvailableContractsMessage(level).sendToAll(level.server)
    }

    private fun vary(value: Double, multiplier: Double): Double {
        val variance = ModConfig.SERVER.variance.get()
        val randomFactor = random.nextDouble()
        val varianceFactor = 1.0 + (randomFactor * 2.0 - 1.0) * variance

        return max(1.0, value * multiplier * varianceFactor)
    }

    fun generateContract(tag: ContractTag): Contract {
        tag.currentCycleStart = currentCycleStart
        tag.startTime = currentCycleStart
        tag.baseUnitsDemanded =
            vary(
                tag.baseUnitsDemanded?.toDouble() ?: 64.0,
                ModConfig.SERVER.defaultUnitsDemandedMultiplier.get()
            ).roundToInt()
        tag.countPerUnit =
            vary(
                tag.countPerUnit?.toDouble() ?: 16.0,
                ModConfig.SERVER.defaultCountPerUnitMultiplier.get()
            ).roundToInt()
        val reward = tag.reward ?: Reward.Random(1.0)
        if (reward is Reward.Random) {
            val rewardValue = vary(reward.value, ModConfig.SERVER.defaultRewardMultiplier.get())
            if (random.nextDouble() <= ModConfig.SERVER.replaceRewardWithRandomPercent.get()) {
                for (_try in 1..5) { // attempt 5 times to find a working item
                    val otherContract = AvailableContractsManager.randomTag()
                    val otherContractItem = otherContract.targetItems?.get(0) ?: continue
                    if (otherContract.targetItems?.size != 1) { // only use contracts that accept exactly one item
                        continue
                    }

                    if ((otherContract.targetConditions?.size ?: 0) != 0) {
                        continue
                    }

                    if (ModConfig.SERVER.replaceRewardWithRandomBlocklist.get()
                            .contains(otherContractItem.`arch$registryName`().toString())
                    ) {
                        continue
                    }

                    if (otherContractItem.asItem() == Items.AIR) {
                        continue
                    }

                    if (tag.targetItems?.size == 1 && otherContractItem.asItem() == tag.targetItems?.get(0)) {
                        continue
                    }

                    val otherContractReward = otherContract.reward
                    if (otherContractReward !is Reward.Random) {
                        continue
                    }

                    val otherContractCountPerUnit = otherContract.countPerUnit?.toDouble() ?: 16.0
                    val otherContractRewardValue = (otherContractReward.value
                            * ModConfig.SERVER.defaultRewardMultiplier.get())
                    val newRewardCount = (rewardValue.pow(2)
                            * otherContractCountPerUnit
                            * ModConfig.SERVER.replaceRewardWithRandomFactor.get()
                            / otherContractRewardValue.pow(2)
                            ).roundToInt()

                    if (newRewardCount <= 0 || newRewardCount >= 128) { // skip contracts that are too far off from a fair reward
                        continue
                    }

                    tag.reward = Reward.Defined(ItemStack(otherContractItem, newRewardCount))
                    break
                }
            }

            if (tag.reward is Reward.Random) {
                tag.reward = Reward.Defined(getRandomReward(rewardValue))
            }

            tag.rarity = tag.rarity ?: AbyssalContract.load(tag, this).calculateRarity(this, reward.value)
        }

        return AbyssalContract.load(tag, this)
    }

    private fun getCount(reward: RewardBagEntry, value: Double) = round(value / reward.value).toInt()
    fun getRandomReward(value: Double): ItemStack {
        if (defaultRewardBagWeightSum <= 0) {
            WingsContractsMod.LOGGER.warn("Default rewards bag weight sum is $defaultRewardBagWeightSum. Should be positive.")
            return FALLBACK_REWARD.item.copy()
        }

        val pick = random.nextInt(defaultRewardBagWeightSum)
        var runningWeight = 0
        for (reward in defaultRewards) {
            runningWeight += reward.weight
            if (pick < runningWeight) {
                val count = getCount(reward, value)
                if (count >= 1) {
                    return reward.item.copyWithCount(reward.item.count * count)
                }
            }
        }

        val lastFit = defaultRewards.lastOrNull { getCount(it, value) >= 1 }
        return if (lastFit == null) {
            defaultRewards.minBy { it.value }.item.copy()
        } else {
            return lastFit.item.copyWithCount(lastFit.item.count * getCount(lastFit, value))
        }
    }

    companion object {
        const val CONTRACT_LIST = "contractList"
        const val REMAINING_PICKS = "remainingPicks"
        const val CURRENT_CYCLE_START = "currentCycleStart"
        const val IDENTIFIER = "${WingsContractsMod.MOD_ID}_world_data"

        val FALLBACK_REWARD = RewardBagEntry(ItemStack(Items.EMERALD, 1), 1.0, 1)

        val random = Random()

        var fakeData = AvailableContractsData()

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

        fun set(world: Level, data: AvailableContractsData) {
            fakeData = data
            if (world !is ServerLevel) {
                return
            }

            world.server.getLevel(Level.OVERWORLD)!!.dataStorage.set(
                IDENTIFIER, data
            )
        }

        fun get(world: Level): AvailableContractsData {
            if (world !is ServerLevel) {
                return fakeData
            }

            val data = world.server.getLevel(Level.OVERWORLD)!!.dataStorage.computeIfAbsent(
                AvailableContractsData::load, ::AvailableContractsData, IDENTIFIER
            )

            return data
        }

        fun remainingPicks(player: Player): Int {
            val remainingPicksMap = get(player.level()).remainingPicks
            val id = player.uuid.toString()
            return remainingPicksMap.computeIfAbsent(id) { ModConfig.SERVER.availableContractsPoolPicks.get() }
        }

        fun setRemainingPicks(player: Player, picks: Int, update: Boolean = false) {
            val remainingPicksMap = get(player.level()).remainingPicks
            val id = player.uuid.toString()
            remainingPicksMap[id] = picks

            val level = player.level()
            if (update && level is ServerLevel) {
                SyncAvailableContractsMessage(level).sendToAll(level.server)
            }
        }

        fun addRemainingPicks(player: Player, picks: Int, update: Boolean = false) {
            val remainingPicksMap = get(player.level()).remainingPicks
            val id = player.uuid.toString()
            remainingPicksMap[id] = (remainingPicksMap[id] ?: 0) + picks

            val level = player.level()
            if (update && level is ServerLevel) {
                SyncAvailableContractsMessage(level).sendToAll(level.server)
            }
        }
    }
}