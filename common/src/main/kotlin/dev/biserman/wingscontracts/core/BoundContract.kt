package dev.biserman.wingscontracts.core

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import dev.biserman.wingscontracts.compat.computercraft.DetailsHelper.details
import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.data.LoadedContracts
import dev.biserman.wingscontracts.nbt.ContractTag
import dev.biserman.wingscontracts.nbt.ContractTagHelper
import dev.biserman.wingscontracts.nbt.ContractTagHelper.uuid
import dev.biserman.wingscontracts.nbt.ItemCondition
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import java.util.*
import kotlin.math.min
import kotlin.reflect.full.memberProperties

class BoundContract(
    id: UUID,
    targetItems: List<Item>,
    targetTags: List<TagKey<Item>>,
    targetBlockTags: List<TagKey<Block>>,
    targetConditions: List<ItemCondition>,

    startTime: Long,

    countPerUnit: Int,
    unitsFulfilledEver: Long,

    author: String,
    name: String?,

    val matchingContractId: UUID,
) : Contract(
    1,
    id,
    targetItems,
    targetTags,
    targetBlockTags,
    targetConditions,
    startTime,
    countPerUnit,
    unitsFulfilledEver,
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

    override fun tryConsumeFromItems(tag: ContractTag, portal: ContractPortalBlockEntity): List<ItemStack> {
        val otherPortal = PortalLinker.get(portal.level!!).linkedPortals[matchingContractId] ?: return listOf()
        val otherTag = ContractTagHelper.getContractTag(otherPortal.contractSlot) ?: return listOf()
        val otherContract = LoadedContracts[otherTag] ?: return listOf()

        val unitCount = min(
            countConsumableUnits(portal.cachedInput.items),
            otherContract.countConsumableUnits(otherPortal.cachedInput.items)
        )
        if (unitCount == 0) {
            return listOf()
        }

        val consumedItems = consumeUnits(unitCount, portal)
        val otherConsumedItems = otherContract.consumeUnits(unitCount, otherPortal)

        for (consumedItem in consumedItems) {
            otherPortal.cachedRewards.addItem(consumedItem)
        }

        unitsFulfilledEver += unitCount
        tag.unitsFulfilledEver = unitsFulfilledEver

        otherContract.unitsFulfilledEver += unitCount
        otherTag.unitsFulfilledEver = unitsFulfilledEver

        return otherConsumedItems
    }

    override fun save(nbt: CompoundTag?): ContractTag {
        val tag = super.save(nbt)
        tag.matchingContractId = matchingContractId
        return tag
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

    companion object {
        var (ContractTag).matchingContractId by uuid()

        fun load(contract: ContractTag): BoundContract {
            return BoundContract(
                id = contract.id ?: UUID.randomUUID(),
                targetItems = contract.targetItems ?: listOf(),
                targetTags = contract.targetTags ?: listOf(),
                targetBlockTags = contract.targetBlockTags ?: listOf(),
                targetConditions = contract.targetConditions ?: listOf(),
                startTime = contract.startTime ?: System.currentTimeMillis(),
                countPerUnit = contract.countPerUnit ?: 64,
                unitsFulfilledEver = contract.unitsFulfilledEver ?: 0,
                author = contract.author ?: ModConfig.SERVER.defaultAuthor.get(),
                name = contract.name,
                matchingContractId = contract.matchingContractId ?: UUID.randomUUID()
            )
        }
    }
}
