package dev.biserman.wingscontracts.api

import dev.biserman.wingscontracts.compat.computercraft.peripherals.DetailsHelper.details
import dev.biserman.wingscontracts.tag.ContractTag
import dev.biserman.wingscontracts.tag.ContractTagHelper.double
import dev.biserman.wingscontracts.tag.ContractTagHelper.int
import dev.biserman.wingscontracts.tag.ContractTagHelper.itemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.*
import kotlin.reflect.full.memberProperties

@Suppress("MemberVisibilityCanBePrivate")
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
    override val displayName: String
        get() = if (level > 0) {
            Component.translatable("item.wingscontracts.contract.abyssal", level, targetName).string
        } else {
            super.displayName
        }

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
        get() = AbyssalContract::class.memberProperties
            .filter { it.name != "details" }
            .associate { prop ->
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

        var (ContractTag).rewardItem: Item?
            get() = reward?.item
            set(value) {
                reward = ItemStack(value ?: Items.AIR, reward?.count ?: 1)
            }

        fun load(contract: ContractTag): AbyssalContract {
            val defaultTargetItems = if (contract.targetItems == null && contract.targetTags == null) {
                listOf(Items.DIRT)
            } else {
                listOf()
            }

            return AbyssalContract(
                id = contract.id ?: UUID.randomUUID(),
                targetItems = contract.targetItems ?: defaultTargetItems,
                targetTags = contract.targetTags ?: listOf(),
                startTime = contract.startTime ?: System.currentTimeMillis(),
                currentCycleStart = contract.currentCycleStart ?: System.currentTimeMillis(),
                cycleDurationMs = contract.cycleDurationMs ?: (1000L * 60 * 5),
                countPerUnit = contract.countPerUnit ?: 64,
                baseUnitsDemanded = contract.baseUnitsDemanded ?: 256,
                unitsFulfilled = contract.unitsFulfilled ?: 0,
                unitsFulfilledEver = contract.unitsFulfilledEver ?: 0,
                isActive = contract.isActive ?: true,
                isLoaded = contract.isLoaded ?: true,
                author = contract.author ?: "",
                reward = contract.reward ?: ItemStack(Items.EMERALD, 1),
                level = contract.level ?: 1,
                quantityGrowthFactor = contract.quantityGrowthFactor ?: 0.5,
                maxLevel = contract.maxLevel ?: 10
            )
        }
    }
}