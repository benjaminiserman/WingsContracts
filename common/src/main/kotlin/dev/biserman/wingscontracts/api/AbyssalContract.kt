package dev.biserman.wingscontracts.api

import net.minecraft.nbt.CompoundTag
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

@Suppress("MemberVisibilityCanBePrivate")
class AbyssalContract : Contract() {
    val rewardItem: Item
    val unitPrice: Int

    var level: Int
    val levelOneQuantity: Int
    val quantityGrowthFactor: Double
    val maxLevel: Int

    override val displayName: String
        get() = if (level > 0) {
            "Level $level $targetName Contract"
        } else {
            super.displayName
        }

    override val unitsDemanded: Int
        get() {
            val quantity = levelOneQuantity + (levelOneQuantity * (level - 1) * quantityGrowthFactor).toInt()
            return quantity - quantity % countPerUnit
        }

    override fun onContractFulfilled() {
        if (level < maxLevel) {
            level += 1
        }
    }

    override fun getRewardsForUnits(units: Int) = ItemStack(rewardItem, unitPrice * units)

    companion object {
        fun load(tag: CompoundTag) {
            Contract.load(tag)
        }
    }
}