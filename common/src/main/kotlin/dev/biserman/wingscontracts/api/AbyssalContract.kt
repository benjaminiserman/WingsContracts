package dev.biserman.wingscontracts.api

import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.compat.computercraft.DetailsHelper.details
import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.tag.ContractTag
import dev.biserman.wingscontracts.tag.ContractTagHelper.double
import dev.biserman.wingscontracts.tag.ContractTagHelper.int
import dev.biserman.wingscontracts.tag.ContractTagHelper.itemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.tags.TagKey
import net.minecraft.util.Mth
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.*
import kotlin.reflect.full.memberProperties

@Suppress("MemberVisibilityCanBePrivate", "NullableBooleanElvis")
class AbyssalContract(
    id: UUID, targetItems: List<Item>, targetTags: List<TagKey<Item>>,

    startTime: Long, currentCycleStart: Long, cycleDurationMs: Long,

    countPerUnit: Int, baseUnitsDemanded: Int, unitsFulfilled: Int, unitsFulfilledEver: Long,

    isActive: Boolean, isLoaded: Boolean, author: String,

    val reward: ItemStack,

    var level: Int, val quantityGrowthFactor: Double, val maxLevel: Int
) : Contract(
    1,
    id,
    targetItems,
    targetTags,
    startTime,
    currentCycleStart,
    cycleDurationMs,
    countPerUnit,
    baseUnitsDemanded,
    unitsFulfilled,
    unitsFulfilledEver,
    isActive,
    isLoaded,
    author
) {
    override val displayName: MutableComponent
        get() = Component.translatable("item.${WingsContractsMod.MOD_ID}.contract.abyssal", level, targetName)

    override fun getBasicInfo(list: MutableList<Component>?): MutableList<Component> {
        val components = list ?: mutableListOf()

        components.add(
            translateContract(
                "abyssal.rewards", reward.count, reward.displayName.string, countPerUnit, listTargets()
            )
        )

        return super.getBasicInfo(components)
    }

    override val unitsDemanded: Int
        get() {
            if (countPerUnit == 0) {
                return 0
            }

            val quantity = baseUnitsDemanded + (baseUnitsDemanded * (level - 1) * quantityGrowthFactor).toInt()
            return quantity - quantity % countPerUnit
        }

    override fun onContractFulfilled(tag: ContractTag?) {
        if (level < maxLevel) {
            level += 1
            tag?.level = level
        }
    }

    override fun getRewardsForUnits(units: Int) = ItemStack(reward.item, reward.count * units)

    override fun save(nbt: CompoundTag?): ContractTag {
        val tag = super.save(nbt)

        tag.reward = reward
        tag.level = level
        tag.quantityGrowthFactor = quantityGrowthFactor
        tag.maxLevel = maxLevel

        return tag
    }

    override val details
        get() = AbyssalContract::class.memberProperties.filter { it.name != "details" }.associate { prop ->
            return@associate Pair(
                prop.name, when (prop.name) {
                "targetItems" -> targetItems.map { it.defaultInstance.details }
                "targetTags" -> targetTags.map { "#${it.location}" }
                "reward" -> reward.details
                else -> prop.get(this)
            })
        }.toMutableMap()

    companion object {
        var (ContractTag).reward by itemStack()

        var (ContractTag).level by int()
        var (ContractTag).quantityGrowthFactor by double()
        var (ContractTag).maxLevel by int()

        fun load(contract: ContractTag): AbyssalContract {
            val tagReward = contract.reward
            val reward = if (tagReward == null) {
                WingsContractsMod.LOGGER.info("creating default reward... ${ModConfig.SERVER.defaultRewardCurrencyId.get()}")
                ItemStack(
                    ModConfig.SERVER.defaultRewardCurrency, Mth.ceil(
                        ModConfig.SERVER.defaultRewardCurrencyMultiplier.get()
                    )
                )
            } else {
                tagReward
            }

            return AbyssalContract(
                id = contract.id ?: UUID.randomUUID(),
                targetItems = contract.targetItems ?: listOf(),
                targetTags = contract.targetTags ?: listOf(),
                startTime = contract.startTime ?: System.currentTimeMillis(),
                currentCycleStart = contract.currentCycleStart ?: System.currentTimeMillis(),
                cycleDurationMs = contract.cycleDurationMs ?: ModConfig.SERVER.defaultCycleDurationMs.get(),
                countPerUnit = contract.countPerUnit ?: 64,
                baseUnitsDemanded = contract.baseUnitsDemanded ?: 16,
                unitsFulfilled = contract.unitsFulfilled ?: 0,
                unitsFulfilledEver = contract.unitsFulfilledEver ?: 0,
                isActive = contract.isActive ?: true,
                isLoaded = contract.isLoaded ?: true,
                author = contract.author ?: ModConfig.SERVER.defaultAuthor.get(),
                reward = reward,
                level = contract.level ?: 1,
                quantityGrowthFactor = contract.quantityGrowthFactor ?: ModConfig.SERVER.defaultGrowthFactor.get(),
                maxLevel = contract.maxLevel ?: ModConfig.SERVER.defaultMaxLevel.get()
            )
        }

        fun fromJson(json: JsonObject): ContractTag =
            ContractTag(JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json) as CompoundTag)
    }
}