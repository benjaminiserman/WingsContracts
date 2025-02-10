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
    author: String

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
    val rewardItemKey by lazy { string("rewardItem").withDefault("minecraft:emerald") }
    val unitPrice by lazy { int("unitPrice").withDefault(1) }

    val level by lazy { int("level").withDefault(1) }
    val startLevel by lazy { int("startLevel").withDefault(1) }
    val levelOneQuantity by lazy { int("levelOneQuantity").withDefault(256) }
    val quantityGrowthFactor by lazy { float("quantityGrowthFactor").withDefault(0.5f) }
    val maxLevel by lazy { int("maxLevel").withDefault(10) }
    override fun getRewardsForUnits(units: Item) {
        TODO("Not yet implemented")
    }
}