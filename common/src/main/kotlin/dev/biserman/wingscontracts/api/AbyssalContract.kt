package dev.biserman.wingscontracts.api

import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

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
    val startLevel: Int,
    val levelOneQuantity: Int,
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
    override fun getRewardsForUnits(units: Item) {
        TODO("Not yet implemented")
    }
}