package dev.biserman.wingscontracts.api

import com.mojang.blaze3d.vertex.PoseStack
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import dev.biserman.wingscontracts.client.renderer.ContractRenderer
import dev.biserman.wingscontracts.tag.ContractTag
import dev.biserman.wingscontracts.tag.ContractTagHelper.boolean
import dev.biserman.wingscontracts.tag.ContractTagHelper.csv
import dev.biserman.wingscontracts.tag.ContractTagHelper.int
import dev.biserman.wingscontracts.tag.ContractTagHelper.long
import dev.biserman.wingscontracts.tag.ContractTagHelper.string
import dev.biserman.wingscontracts.util.DenominationHelper
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.NonNullList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import org.joml.Vector3d
import java.util.*
import kotlin.math.min

@Suppress("MemberVisibilityCanBePrivate")
abstract class Contract(
    val type: Int = 0,
    val targetItems: List<Item> = listOf(),
    val targetTags: List<TagKey<Item>> = listOf(),

    val startTime: Long = System.currentTimeMillis(),
    var currentCycleStart: Long = System.currentTimeMillis(),
    val cycleDurationMs: Long = 1000L * 60 * 5,

    val countPerUnit: Int = 1,
    val baseUnitsDemanded: Int = 16,
    var unitsFulfilled: Int = 0,
    var unitsFulfilledEver: Long = 0,

    var isActive: Boolean = true,
    val author: String = ""
) {
    open val unitsDemanded = baseUnitsDemanded


    fun matches(itemStack: ItemStack): Boolean {
        if (targetTags.isNotEmpty()) {
            return targetTags.any { itemStack.`is`(it) }
        }

        if (targetItems.isNotEmpty()) {
            return targetItems.any { itemStack.item == it }
        }

        return false
    }

    val allMatchingItems by lazy {
        targetItems.map { it.defaultInstance }
            .plus(targetTags.flatMap {
                BuiltInRegistries.ITEM.getTagOrEmpty(it).map { holder -> holder.value().defaultInstance }
            })
    }

    open val targetName by lazy {
        val targetComponentCount = targetItems.size + targetTags.size
        if (targetComponentCount > 1) {
            return@lazy "Complex"
        }

        if (targetItems.isNotEmpty()) {
            val targetItemDisplayName = Component.translatable(targetItems[0].descriptionId).string
            return@lazy targetItemDisplayName ?: "Unknown"
        }

        if (targetTags.isNotEmpty()) {
            val path = targetTags[0].registry.location().path
            val firstLetterCapital = path.substring(0, 1).uppercase(Locale.getDefault())
            return@lazy "${firstLetterCapital}${path.substring(1)}"
        }

        return@lazy "Empty"
    }

    fun tryUpdateTick(): Boolean {
        if (!isActive) {
            return false
        }

        if (cycleDurationMs <= 0) {
            if (unitsFulfilled >= unitsDemanded) {
                onContractFulfilled()
                isActive = false
                return true
            } else {
                return false
            }
        }

        val currentTime = System.currentTimeMillis()
        val cyclesPassed = ((currentTime - currentCycleStart) / cycleDurationMs).toInt()
        if (cyclesPassed > 0) {
            reset(currentCycleStart + cycleDurationMs * cyclesPassed)
            return true
        }

        return false
    }

    open fun reset(newCycleStart: Long) {
        if (unitsFulfilled >= unitsDemanded) {
            onContractFulfilled()
        }

        currentCycleStart = newCycleStart
        unitsFulfilled = 0
    }

    open fun onContractFulfilled() {}

    open val displayName get() = "$targetName Contract"

    open fun getBasicInfo(): List<Component> {
        val components = mutableListOf<Component>()
        components.add(Component.literal("Units Fulfilled: ${unitsFulfilled}/${unitsDemanded}"))

        return components
    }

    open fun getTimeInfo(): List<Component> {
        val components = mutableListOf<Component>()
        val nextCycleStart = currentCycleStart + cycleDurationMs
        val timeRemaining = DenominationHelper.denominate(
            nextCycleStart - System.currentTimeMillis(),
            DenominationHelper.timeDenominationsWithoutMs
        ).joinToString(separator = ", ") { kvp ->
            "${kvp.second} ${kvp.first}${
                if (kvp.second == 1) {
                    ""
                } else {
                    "s"
                }
            }"
        }
        if (Date(nextCycleStart) <= Date()) {
            components.add(Component.literal("Cycle Complete!"))
            components.add(Component.literal("Completed at ${Date(nextCycleStart)}"))
            components.add(Component.literal("Right-click with contract in hand or place in contract portal to start next cycle."))
        } else {
            components.add(Component.literal("Current Cycle Ends at ${Date(nextCycleStart)}"))
            components.add(Component.literal("Current Cycle Remaining Time:"))
            components.add(Component.literal("    $timeRemaining"))
        }

        return components
    }

    open fun getExtraInfo(
        showExtraInfo: Boolean, extraInfoMessage: String
    ): List<Component> {
        val components = mutableListOf<Component>()
        if (showExtraInfo) {
            components.add(Component.literal("Started On: ${Date(startTime)}"))
            components.add(Component.literal(("Units Fulfilled Ever: $unitsFulfilledEver")))
            components.add(Component.literal(("Total Quantity Fulfilled Ever: ${unitsFulfilledEver * countPerUnit}")))
            components.add(Component.literal("Base Units Demanded: $baseUnitsDemanded"))
        } else {
            components.add(Component.literal(extraInfoMessage))
        }

        return components
    }

    open fun getDescription(showExtraInfo: Boolean, extraInfoMessage: String): List<Component> {
        val components = mutableListOf<Component>()

        components.addAll(getBasicInfo())
        components.addAll(getTimeInfo())
        components.addAll(getExtraInfo(showExtraInfo, extraInfoMessage))

        return components
    }

    open fun countConsumableUnits(items: NonNullList<ItemStack>): Int {
        val matchingStacks = items.filter { !it.isEmpty && matches(it) }
        val matchingCount = matchingStacks.sumOf { it.count }
        return matchingCount / countPerUnit
    }

    open fun tryConsumeFromItems(items: NonNullList<ItemStack>): Int {
        val unitCount = countConsumableUnits(items)
        if (unitCount == 0) {
            return 0
        }

        val goalAmount = unitCount * countPerUnit
        var amountTaken = 0
        for (itemStack in items) {
            if (amountTaken >= goalAmount) {
                break
            }

            if (itemStack.isEmpty) {
                continue
            }

            if (matches(itemStack)) {
                val amountToTake = min(itemStack.count, goalAmount - amountTaken)
                amountTaken += amountToTake
                itemStack.shrink(amountToTake)
            }
        }

        unitsFulfilled += amountTaken
        unitsFulfilledEver += amountTaken

        return amountTaken
    }

    abstract fun getRewardsForUnits(units: Int): ItemStack

    open fun portalRender(
        context: BlockEntityRendererProvider.Context,
        contract: Contract,
        blockEntity: ContractPortalBlockEntity,
        translate: Vector3d,
        partialTick: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource
    ) = ContractRenderer.render(
        context, contract, blockEntity, translate, partialTick, poseStack, multiBufferSource
    )

    open fun save(nbt: CompoundTag? = null): ContractTag {
        val tag = ContractTag(nbt ?: CompoundTag())

        tag.type = type
        tag.targetItems = targetItems
        tag.targetTags = targetTags
        tag.startTime = startTime
        tag.currentCycleStart = currentCycleStart
        tag.cycleDurationMs = cycleDurationMs
        tag.countPerUnit = countPerUnit
        tag.baseUnitsDemanded = baseUnitsDemanded
        tag.unitsFulfilled = unitsFulfilled
        tag.unitsFulfilledEver = unitsFulfilledEver
        tag.isActive = isActive
        tag.author = author

        return tag
    }

    companion object {
        var (ContractTag).type by int()

        var (ContractTag).targetItemKeys by csv("targetItems")
        var (ContractTag).targetTagKeys by csv("targetTags")

        var (ContractTag).startTime by long()
        var (ContractTag).currentCycleStart by long()
        var (ContractTag).cycleDurationMs by long()

        var (ContractTag).countPerUnit by int()
        var (ContractTag).baseUnitsDemanded by int()
        var (ContractTag).unitsFulfilled by int()
        var (ContractTag).unitsFulfilledEver by long()

        var (ContractTag).isActive by boolean()
        var (ContractTag).author by string()


        var (ContractTag).targetTags: List<TagKey<Item>>?
            get() {
                val tagKeys = targetTagKeys ?: return null
                if (tagKeys.isNotEmpty()) {
                    return tagKeys.map {
                        TagKey.create(
                            Registries.ITEM, ResourceLocation.tryParse(it) ?: ResourceLocation("")
                        )
                    }
                }

                return null
            }
            set(value) {
                targetTagKeys = value?.map { it.registry.location().toString() }
            }

        var (ContractTag).targetItems: List<Item>?
            get() {
                val targetItems = targetItemKeys ?: return null
                if (targetItems.isNotEmpty()) {
                    return targetItems.map {
                        BuiltInRegistries.ITEM[ResourceLocation.tryParse(it)]
                    }
                }

                return null
            }
            set(value) {
                targetItemKeys = value?.mapNotNull { it.`arch$registryName`()?.path }
            }
    }
}
