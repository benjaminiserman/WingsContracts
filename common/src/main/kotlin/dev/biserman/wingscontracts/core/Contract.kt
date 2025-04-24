package dev.biserman.wingscontracts.core

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.data.LoadedContracts
import dev.biserman.wingscontracts.registry.ModItemRegistry
import dev.biserman.wingscontracts.tag.ContractTag
import dev.biserman.wingscontracts.tag.ContractTagHelper
import dev.biserman.wingscontracts.tag.ContractTagHelper.boolean
import dev.biserman.wingscontracts.tag.ContractTagHelper.csv
import dev.biserman.wingscontracts.tag.ContractTagHelper.int
import dev.biserman.wingscontracts.tag.ContractTagHelper.long
import dev.biserman.wingscontracts.tag.ContractTagHelper.string
import dev.biserman.wingscontracts.tag.ContractTagHelper.uuid
import dev.biserman.wingscontracts.util.ComponentHelper.trimBrackets
import dev.biserman.wingscontracts.util.DenominationsHelper
import net.minecraft.ChatFormatting
import net.minecraft.core.NonNullList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.util.Mth
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.*
import kotlin.math.min

fun (Item).name(): String = this.defaultInstance.displayName.string
fun (TagKey<Item>).name(): String {
    val path = this.location.path
    val firstLetterCapital = path.substring(0, 1).uppercase(Locale.getDefault())
    return "${firstLetterCapital}${path.substring(1)}"
}

@Suppress("MemberVisibilityCanBePrivate")
abstract class Contract(
    val type: Int = 0,
    val id: UUID = UUID.randomUUID(),
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
    var isLoaded: Boolean = true,
    val author: String = "",
    val name: String? = null,
    val description: String? = null,
    val shortTargetList: String? = null,
    val rarity: Int? = null
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
        targetItems.map { it.defaultInstance }.plus(targetTags.flatMap {
            BuiltInRegistries.ITEM.getTagOrEmpty(it).map { holder -> holder.value().defaultInstance }
        })
    }

    open val targetName: String by lazy {
        val targetComponentCount = targetItems.size + targetTags.size
        if (targetComponentCount > 1) {
            return@lazy translateContract("complex").string
        }

        if (targetItems.isNotEmpty()) {
            return@lazy targetItems[0].name().trimBrackets()
        }

        if (targetTags.isNotEmpty()) {
            return@lazy targetTags[0].name()
                .split("/")
                .reversed()
                .flatMap { it.split("_") }
                .joinToString(" ") { it.replaceFirstChar { it.titlecase(Locale.getDefault()) } }
        }

        return@lazy translateContract("empty").string
    }

    fun tryUpdateTick(tag: ContractTag?): Boolean {
        if (!isActive) {
            return false
        }

        if (cycleDurationMs <= 0) {
            if (unitsFulfilled >= unitsDemanded) {
                onContractFulfilled(tag)
                isActive = false
                tag?.isActive = isActive
                return true
            } else {
                return false
            }
        }

        val currentTime = System.currentTimeMillis()
        val cyclesPassed = ((currentTime - currentCycleStart) / cycleDurationMs).toInt()
        if (cyclesPassed > 0) {
            reset(tag, currentCycleStart + cycleDurationMs * cyclesPassed)
            return true
        }

        return false
    }

    open fun reset(tag: ContractTag?, newCycleStart: Long) {
        if (unitsFulfilled >= unitsDemanded) {
            onContractFulfilled(tag)
        }

        currentCycleStart = newCycleStart
        tag?.currentCycleStart = currentCycleStart
        unitsFulfilled = 0
        tag?.unitsFulfilled = unitsFulfilled
    }

    open fun onContractFulfilled(tag: ContractTag?) {}

    open val displayName: MutableComponent
        get() = Component.translatable(
            "item.${WingsContractsMod.MOD_ID}.contract", Component.translatable(name ?: targetName).string
        )

    fun listTargets(displayShort: Boolean): String {
        val totalSize = targetItems.size + targetTags.size

        val separator = if (displayShort) "|" else "\n"
        val complexPrefix = if (displayShort) "" else " - "
        val tagKey = if (displayShort) "items_of_tag_short" else "items_of_tag"
        val complexKey = if (displayShort) "matches_following_short" else "matches_following"

        if (displayShort) {
            if (shortTargetList != null) {
                return Component.translatable(shortTargetList).string
            } else if (totalSize > 3) {
                return Component.translatable(name ?: targetName).string
            }
        }

        return when (totalSize) {
            0 -> translateContract("no_targets").string
            1 -> if (targetItems.isNotEmpty()) {
                targetItems[0].name().trimBrackets()
            } else {
                translateContract(tagKey, targetTags[0].location()).string
            }
            else -> translateContract(
                complexKey,
                targetItems.asSequence()
                    .map { it.name().trimBrackets() }
                    .plus(targetTags.map { it.name() })
                    .joinToString(separator = separator) { "$complexPrefix$it" })
                .string
        }
    }

    open fun getBasicInfo(list: MutableList<Component>?): MutableList<Component> {
        val components = list ?: mutableListOf()
        components.add(
            translateContract(
                "units_fulfilled",
                unitsFulfilled,
                unitsDemanded,
                unitsFulfilled * countPerUnit,
                unitsDemanded * countPerUnit
            ).withStyle(ChatFormatting.LIGHT_PURPLE)
        )

        return components
    }

    open fun getTimeInfo(list: MutableList<Component>?): MutableList<Component> {
        val components = mutableListOf<Component>()
        val nextCycleStart = currentCycleStart + cycleDurationMs
        val timeRemaining = nextCycleStart - System.currentTimeMillis()
        val timeRemainingString = DenominationsHelper.denominateDurationToString(timeRemaining)

        val timeRemainingColor = when {
            timeRemaining < 1000 * 60 * 60 -> ChatFormatting.RED
            timeRemaining < 1000 * 60 * 60 * 24 -> ChatFormatting.YELLOW
            else -> ChatFormatting.DARK_PURPLE
        }

        if (Date(nextCycleStart) <= Date()) {
            components.add(translateContract("cycle_complete").withStyle(ChatFormatting.DARK_PURPLE))
        } else {
            components.add(translateContract("cycle_remaining").withStyle(timeRemainingColor))
            components.add(Component.literal("  $timeRemainingString").withStyle(timeRemainingColor))
        }

        return components
    }

    open fun getShortInfo(): Component = Component.empty()

    open fun getExtraInfo(
        list: MutableList<Component>?
    ): MutableList<Component> {
        val components = mutableListOf<Component>()

        components.addAll(getBasicInfo(null))
        components.addAll(getTimeInfo(null))
        components.add(
            translateContract(
                "total_fulfilled", unitsFulfilledEver, unitsFulfilledEver * countPerUnit
            ).withStyle(ChatFormatting.LIGHT_PURPLE)
        )

        if (author.isNotBlank()) {
            components.add(translateContract("author", author).withStyle(ChatFormatting.DARK_PURPLE))
        }

        return components
    }

    open fun getDescription(
        showExtraInfo: Boolean, howExtraInfo: Component
    ): MutableList<Component> {
        val components = mutableListOf<Component>()

        if (showExtraInfo) {
            components.addAll(getExtraInfo(null))
        } else {
            components.add(getShortInfo())
            if (!description.isNullOrBlank()) {
                components.add(Component.translatable(description).withStyle(ChatFormatting.GRAY))
            }
            components.add(howExtraInfo)
        }

        return components
    }

    open fun countConsumableUnits(items: NonNullList<ItemStack>): Int {
        val matchingStacks = items.filter { !it.isEmpty && matches(it) }
        val matchingCount = matchingStacks.sumOf { it.count }
        return min(matchingCount / countPerUnit, unitsDemanded - unitsFulfilled)
    }

    open fun tryConsumeFromItems(tag: ContractTag?, items: NonNullList<ItemStack>): Int {
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

        unitsFulfilled += unitCount
        tag?.unitsFulfilled = unitsFulfilled
        unitsFulfilledEver += unitCount
        tag?.unitsFulfilledEver = unitsFulfilledEver

        return unitCount
    }

    abstract fun getRewardsForUnits(units: Int): ItemStack

    open fun getRarity() = rarity ?: 0

    open fun save(nbt: CompoundTag? = null): ContractTag {
        val tag = ContractTag(nbt ?: CompoundTag())

        tag.type = type
        tag.id = id
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
        tag.isLoaded = isLoaded
        tag.author = author
        tag.name = name
        tag.description = description
        tag.shortTargetList = shortTargetList
        tag.rarity = rarity

        return tag
    }

    fun createItem(): ItemStack {
        val itemStack = ItemStack(ModItemRegistry.ABYSSAL_CONTRACT.get() ?: throw Error())
        val tag = save(null)
        ContractTagHelper.setContractTag(itemStack, tag)
        LoadedContracts.update(this)

        return itemStack
    }

    abstract val details: MutableMap<String, Any?>

    companion object {
        var (ContractTag).type by int()
        var (ContractTag).id by uuid()

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
        var (ContractTag).isLoaded by boolean()
        var (ContractTag).author by string()
        var (ContractTag).name by string()
        var (ContractTag).description by string()
        var (ContractTag).shortTargetList by string()
        var (ContractTag).rarity by int()

        var (ContractTag).targetTags: List<TagKey<Item>>?
            get() {
                val tagKeys = targetTagKeys ?: return null

                if (tagKeys.isNotEmpty()) {
                    return tagKeys.map { it.trimStart('#') }.mapNotNull {
                        return@mapNotNull TagKey.create(
                            Registries.ITEM, ResourceLocation.tryParse(it) ?: return@mapNotNull null
                        )
                    }
                }

                return null
            }
            set(value) {
                targetTagKeys = value?.map { it.location().toString() }
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
                targetItemKeys = value?.mapNotNull { it.`arch$registryName`()?.toString() }
            }

        fun translateContract(key: String, vararg objects: Any): MutableComponent =
            Component.translatable("${WingsContractsMod.MOD_ID}.contract.$key", *objects)

        fun getDisplayItem(itemStack: ItemStack, time: Float): ItemStack {
            val contract = LoadedContracts[itemStack] ?: return ItemStack.EMPTY

            return if (contract.allMatchingItems.isEmpty()) {
                ItemStack.EMPTY
            } else {
                contract.allMatchingItems[Mth.floor(time / 30.0f) % contract.allMatchingItems.size]
            }
        }
    }
}
