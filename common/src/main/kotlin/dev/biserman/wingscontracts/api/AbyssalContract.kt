package dev.biserman.wingscontracts.api

import dev.biserman.wingscontracts.tag.ContractTag
import dev.biserman.wingscontracts.tag.ContractTagHelper
import net.minecraft.nbt.CompoundTag
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

@Suppress("MemberVisibilityCanBePrivate")
class AbyssalContract(
    targetItems: List<Item>,
    targetTags: List<TagKey<Item>>,

    startTime: Long,
    currentCycleStart: Long,
    cycleDurationMs: Long,

    countPerUnit: Int,
    baseUnitsDemanded: Int,
    unitsFulfilled: Int,
    unitsFulfilledEver: Long,

    isActive: Boolean,
    author: String,

    val rewardItem: Item,
    val unitPrice: Int,

    var level: Int,
    val quantityGrowthFactor: Double,
    val maxLevel: Int
) : Contract(
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
    author
) {
    override val displayName: String
        get() = if (level > 0) {
            "Level $level $targetName Contract"
        } else {
            super.displayName
        }

    override val unitsDemanded: Int
        get() {
            val quantity = baseUnitsDemanded + (baseUnitsDemanded * (level - 1) * quantityGrowthFactor).toInt()
            return quantity - quantity % countPerUnit
        }

    override fun onContractFulfilled() {
        if (level < maxLevel) {
            level += 1
        }
    }

    override fun getRewardsForUnits(units: Int) = ItemStack(rewardItem, unitPrice * units)

    fun (ContractTagHelper).loadAbyssal(itemStack: ItemStack): AbyssalContract? {
        val contract = ContractTag.from(itemStack) ?: return null
        val defaultTargetItems = if (contract.targetItems == null && contract.targetTags == null) {
            listOf(Items.DIRT)
        } else {
            listOf()
        }

        return AbyssalContract(
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
            author = contract.author ?: "",
            rewardItem = contract.rewardItem ?: Items.EMERALD,
            unitPrice = contract.unitPrice ?: 1,
            level = contract.level ?: 1,
            quantityGrowthFactor = contract.quantityGrowthFactor ?: 0.5,
            maxLevel = contract.maxLevel ?: 10
        )
    }
}