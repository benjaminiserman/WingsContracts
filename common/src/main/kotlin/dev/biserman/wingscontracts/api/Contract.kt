package dev.biserman.wingscontracts.api

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
import dev.biserman.wingscontracts.util.DenominationsHelper
import net.minecraft.core.NonNullList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
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
        targetItems.map { it.defaultInstance }.plus(targetTags.flatMap {
            BuiltInRegistries.ITEM.getTagOrEmpty(it).map { holder -> holder.value().defaultInstance }
        })
    }

    open val targetName by lazy {
        val targetComponentCount = targetItems.size + targetTags.size
        if (targetComponentCount > 1) {
            return@lazy translateContract("complex").string
        }

        if (targetItems.isNotEmpty()) {
            return@lazy targetItems[0].name()
        }

        if (targetTags.isNotEmpty()) {
            return@lazy targetTags[0].name()
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

    open val displayName get() = Component.translatable("item.${WingsContractsMod.MOD_ID}.contract", targetName)

    fun listTargets(): String {

        val totalSize = targetItems.size + targetTags.size
        return when (totalSize) {
            0 -> translateContract("no_targets").string
            1 -> if (targetItems.isNotEmpty()) {
                targetItems[0].name()
            } else {
                translateContract("items_of_tag", targetTags[0].name()).string
            }

            else -> translateContract(
                "matches_following",
                targetItems.asSequence().map { it.name() }
                    .plus(targetTags.map { it.name() })
                    .joinToString(separator = "\n") { " - $it" }
            ).string
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
            )
        )

        return components
    }

    open fun getTimeInfo(list: MutableList<Component>?): MutableList<Component> {
        val components = mutableListOf<Component>()
        val nextCycleStart = currentCycleStart + cycleDurationMs
        val timeRemaining = DenominationsHelper.denominate(
            nextCycleStart - System.currentTimeMillis(), DenominationsHelper.timeDenominationsWithoutMs
        ).asSequence().joinToString(separator = ", ") { kvp ->
            "${kvp.second} ${kvp.first}${
                if (kvp.second == 1) {
                    ""
                } else {
                    "s"
                }
            }"
        }
        if (Date(nextCycleStart) <= Date()) {
            components.add(translateContract("cycle_complete"))
        } else {
            components.add(translateContract("cycle_remaining", Date(nextCycleStart), timeRemaining))
        }

        return components
    }

    open fun getExtraInfo(
        list: MutableList<Component>?, showExtraInfo: Boolean, extraInfoMessage: String
    ): MutableList<Component> {
        val components = mutableListOf<Component>()
        if (showExtraInfo) {
            components.add(translateContract("cycle_started", Date(startTime)))
            components.add(translateContract("total_fulfilled", unitsFulfilledEver, unitsFulfilledEver * countPerUnit))
            components.add(
                translateContract(
                    "base_units_demanded",
                    baseUnitsDemanded,
                    baseUnitsDemanded * countPerUnit
                )
            )

            if (author.isNotBlank()) {
                components.add(translateContract("author", author))
            }
        } else {
            components.add(Component.literal(extraInfoMessage))
        }

        return components
    }

    open fun getDescription(
        showExtraInfo: Boolean, extraInfoMessage: String
    ): MutableList<Component> {
        val components = mutableListOf<Component>()

        components.addAll(getBasicInfo(null))
        components.addAll(getTimeInfo(null))
        components.addAll(getExtraInfo(null, showExtraInfo, extraInfoMessage))

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

        return tag
    }

    fun createItem(): ItemStack {
        val itemStack = ItemStack(ModItemRegistry.CONTRACT.get() ?: throw Error())
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

        var (ContractTag).targetTags: List<TagKey<Item>>?
            get() {
                val tagKeys = targetTagKeys ?: return null

                if (tagKeys.isNotEmpty()) {
                    return tagKeys.map { it.trimStart('#') }.mapNotNull {
                        WingsContractsMod.LOGGER.info("YTTER found tag $it")
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
                targetItemKeys = value?.mapNotNull { it.`arch$registryName`()?.path }
            }

        fun translateContract(key: String, vararg objects: Any): Component =
            Component.translatable("${WingsContractsMod.MOD_ID}.contract.$key", *objects)

    }
}
