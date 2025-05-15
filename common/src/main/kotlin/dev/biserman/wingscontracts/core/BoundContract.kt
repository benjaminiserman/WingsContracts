package dev.biserman.wingscontracts.core

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.compat.computercraft.DetailsHelper.details
import dev.biserman.wingscontracts.nbt.ContractTag
import net.minecraft.core.NonNullList
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.*
import kotlin.reflect.full.memberProperties

class BoundContract(
    id: UUID,
    targetItems: List<Item>,

    startTime: Long,

    countPerUnit: Int,
    unitsFulfilledEver: Long,

    isActive: Boolean,
    isLoaded: Boolean,
    author: String,
    name: String?,

    val matchingContractId: UUID,
) : Contract(
    1,
    id,
    targetItems,
    listOf(),
    listOf(),
    listOf(),
    startTime,
    countPerUnit,
    unitsFulfilledEver,
    isActive,
    isLoaded,
    author,
    name,
    null,
    null,
    null,
    null
) {
    override val displayName: MutableComponent
        get() {
            val nameString = Component.translatable(name ?: targetName).string

            return Component.translatable(
                "item.${WingsContractsMod.MOD_ID}.contract.bound",
                nameString,
            )
        }

    override fun getRewardsForUnits(units: Int): ItemStack {
        TODO("Not yet implemented")
    }

    override fun tryConsumeFromItems(tag: ContractTag?, items: NonNullList<ItemStack>): Int {
        TODO("manage the linking")
        return super.tryConsumeFromItems(tag, items)
    }

    override val details
        get() = BoundContract::class.memberProperties
            .filter { it.name != "details" }
            .associate { prop ->
                return@associate Pair(
                    prop.name, when (prop.name) {
                        "targetItems" -> targetItems.map { it.defaultInstance.details }
                        "targetTags" -> targetTags.map { "#${it.location}" }
                        "targetBlockTags" -> targetBlockTags.map { "#${it.location}" }
                        else -> prop.get(this)
                    })
            }.toMutableMap()
}
