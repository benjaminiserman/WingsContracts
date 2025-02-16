package dev.biserman.wingscontracts.api

import dev.biserman.wingscontracts.tag.ContractTag
import dev.biserman.wingscontracts.tag.ContractTagHelper.double
import dev.biserman.wingscontracts.tag.ContractTagHelper.int
import dev.biserman.wingscontracts.tag.ContractTagHelper.string
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class AbyssalContract(
    id: UUID,
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
    isLoaded: Boolean,
    author: String,

    val rewardItem: Item,
    val unitPrice: Int,

    var level: Int,
    val quantityGrowthFactor: Double,
    val maxLevel: Int
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
            "Level $level $targetName Contract"
        } else {
            super.displayName
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

    override fun getRewardsForUnits(units: Int) = ItemStack(rewardItem, unitPrice * units)

    override fun save(nbt: CompoundTag?): ContractTag {
        val tag = super.save(nbt)

        tag.rewardItem = rewardItem
        tag.unitPrice = unitPrice
        tag.level = level
        tag.quantityGrowthFactor = quantityGrowthFactor
        tag.maxLevel = maxLevel

        return tag
    }

    companion object {
        var (ContractTag).rewardItemKey by string("rewardItem")
        var (ContractTag).unitPrice by int()

        var (ContractTag).level by int()
        var (ContractTag).quantityGrowthFactor by double()
        var (ContractTag).maxLevel by int()

        var (ContractTag).rewardItem: Item?
            get() {
                val rewardItem = rewardItemKey ?: return null
                if (rewardItem.isNotEmpty()) {
                    return BuiltInRegistries.ITEM[ResourceLocation.tryParse(rewardItem)]
                }

                return null
            }
            set(value) {
                rewardItemKey = value?.`arch$registryName`()?.path
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
                rewardItem = contract.rewardItem ?: Items.EMERALD,
                unitPrice = contract.unitPrice ?: 1,
                level = contract.level ?: 1,
                quantityGrowthFactor = contract.quantityGrowthFactor ?: 0.5,
                maxLevel = contract.maxLevel ?: 10
            )
        }
    }
}