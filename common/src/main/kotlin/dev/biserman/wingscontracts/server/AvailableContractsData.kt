@file:OptIn(ExperimentalStdlibApi::class)

package dev.biserman.wingscontracts.server

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.config.DenominatedCurrenciesHandler
import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.core.AbyssalContract
import dev.biserman.wingscontracts.core.AbyssalContract.Companion.reward
import dev.biserman.wingscontracts.core.Contract
import dev.biserman.wingscontracts.core.Contract.Companion.baseUnitsDemanded
import dev.biserman.wingscontracts.core.Contract.Companion.countPerUnit
import dev.biserman.wingscontracts.core.Contract.Companion.currentCycleStart
import dev.biserman.wingscontracts.core.Contract.Companion.rarity
import dev.biserman.wingscontracts.core.Contract.Companion.startTime
import dev.biserman.wingscontracts.core.Contract.Companion.targetItems
import dev.biserman.wingscontracts.data.AvailableContractsManager
import dev.biserman.wingscontracts.nbt.ContractTag
import dev.biserman.wingscontracts.nbt.Reward
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import java.util.*
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt

class RewardBagEntry(val item: Item, val value: Double, val weight: Int, val formatString: String? = null)

class AvailableContractsData : SavedData() {
    val container = AvailableContractsContainer(this)
    val remainingPicks = mutableMapOf<String, Int>()
    var currentCycleStart: Long = 0
    val nextCycleStart get() = currentCycleStart + ModConfig.SERVER.availableContractsPoolRefreshPeriodMs.get()

    val rarityThresholds by lazy { ModConfig.SERVER.rarityThresholdsString.get().split(",").map { it.toInt() } }
    val currencyHandler by lazy { DenominatedCurrenciesHandler() }
    val defaultRewards by lazy {
        ModConfig.SERVER.defaultRewards.get().split(";").map {
            val (item, value, weight, formatString) = it.split(",")
            RewardBagEntry(
                BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(item)),
                value.toDouble(),
                weight.toInt(),
                formatString
            )
        }.sortedBy { it.value }
    }
    val defaultRewardBagWeightSum by lazy { defaultRewards.sumOf { it.weight } }

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
        remainingPicks.keys.forEach { remainingPicks[it] = ModConfig.SERVER.availableContractsPoolPicks.get() }

        container.clearContent()
        for (i in 0..<container.containerSize) {
            container.items[i] = generateContract(AvailableContractsManager.randomTag()).createItem()
        }

        SyncAvailableContractsMessage(level).sendToAll(level.server)
    }

    private fun vary(value: Int, multiplier: Double): Int {
        val variance = ModConfig.SERVER.variance.get()
        val randomFactor = random.nextDouble()
        val varianceFactor = 1.0 + (randomFactor * 2.0 - 1.0) * variance

        return max(1.0, value.toDouble() * multiplier * varianceFactor).roundToInt()
    }

    fun generateContract(tag: ContractTag): Contract {
        tag.currentCycleStart = currentCycleStart
        tag.startTime = currentCycleStart
        tag.baseUnitsDemanded = vary(tag.baseUnitsDemanded ?: 64, ModConfig.SERVER.defaultUnitsDemandedMultiplier.get())
        tag.countPerUnit = vary(tag.countPerUnit ?: 16, ModConfig.SERVER.defaultCountPerUnitMultiplier.get())
        val reward = tag.reward
        if (reward is Reward.Random) {
            val rewardCount = vary(reward.count, ModConfig.SERVER.defaultRewardCurrencyMultiplier.get())
            if (random.nextDouble() <= ModConfig.SERVER.replaceRewardWithRandomPercent.get()) {
                for (_try in 1..5) { // attempt 5 times to find a working item
                    val otherContract = AvailableContractsManager.randomTag()
                    val otherContractItem = otherContract.targetItems?.get(0) ?: continue
                    if (otherContract.targetItems?.size != 1) { // only use contracts that accept exactly one item
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
                    val otherContractRewardCount = (otherContractReward.count.toDouble()
                            * ModConfig.SERVER.defaultRewardCurrencyMultiplier.get())
                    val newRewardCount = (rewardCount.toDouble().pow(2)
                            * otherContractCountPerUnit
                            * ModConfig.SERVER.replaceRewardWithRandomFactor.get()
                            / otherContractRewardCount.pow(2)
                            ).roundToInt()

                    if (newRewardCount <= 0 || newRewardCount >= 128) { // skip contracts that are too far off from a fair reward
                        continue
                    }

                    tag.rarity = AbyssalContract.load(tag, this).getRarity()
                    tag.reward = Reward.Defined(ItemStack(otherContractItem, newRewardCount))
                    break
                }
            } else {
                tag.reward = Reward.Defined(getRandomReward(reward.count))
            }
        }

        return AbyssalContract.load(tag, this)
    }

    private fun getCount(reward: RewardBagEntry, value: Int) = round(value / reward.value).toInt()
    fun getRandomReward(value: Int): ItemStack {
        val pick = random.nextInt(defaultRewardBagWeightSum)
        var runningWeight = 0
        for (reward in defaultRewards) {
            runningWeight += reward.weight
            if (pick < runningWeight) {
                val count = getCount(reward, value)
                if (count >= 1) {
                    return ItemStack(reward.item, count)
                }
            }
        }

        val lastFit = defaultRewards.lastOrNull { getCount(it, value) >= 1 }
        return if (lastFit == null) {
            ItemStack(FALLBACK_REWARD.item, 1)
        } else {
            ItemStack(lastFit.item, getCount(lastFit, value))
        }
    }

    companion object {
        const val CONTRACT_LIST = "contractList"
        const val REMAINING_PICKS = "remainingPicks"
        const val CURRENT_CYCLE_START = "currentCycleStart"
        const val IDENTIFIER = "${WingsContractsMod.MOD_ID}_world_data"

        val FALLBACK_REWARD = RewardBagEntry(Items.EMERALD, 1.0, 1)

        val random = Random()

        @Environment(EnvType.CLIENT)
        var clientData = AvailableContractsData()

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
            clientData = data
            if (world !is ServerLevel) {
                return
            }

            world.server.getLevel(Level.OVERWORLD)!!.dataStorage.set(
                IDENTIFIER, data
            )
        }

        fun get(world: Level): AvailableContractsData {
            if (world !is ServerLevel) {
                return clientData
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

        fun setRemainingPicks(player: Player, picks: Int) {
            val remainingPicksMap = get(player.level()).remainingPicks
            val id = player.uuid.toString()
            remainingPicksMap[id] = picks
        }
    }
}