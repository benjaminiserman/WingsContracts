package dev.biserman.wingscontracts.core

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import dev.biserman.wingscontracts.compat.computercraft.DetailsHelper.details
import dev.biserman.wingscontracts.config.GrowthFunctionOptions
import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.data.AvailableContractsManager
import dev.biserman.wingscontracts.nbt.ContractTag
import dev.biserman.wingscontracts.nbt.ContractTagHelper.boolean
import dev.biserman.wingscontracts.nbt.ContractTagHelper.double
import dev.biserman.wingscontracts.nbt.ContractTagHelper.int
import dev.biserman.wingscontracts.nbt.ContractTagHelper.long
import dev.biserman.wingscontracts.nbt.ContractTagHelper.reward
import dev.biserman.wingscontracts.nbt.ItemCondition
import dev.biserman.wingscontracts.nbt.Reward
import dev.biserman.wingscontracts.registry.ModItemRegistry
import dev.biserman.wingscontracts.server.AvailableContractsData
import dev.biserman.wingscontracts.util.ComponentHelper.trimBrackets
import dev.biserman.wingscontracts.util.DenominationsHelper
import net.minecraft.ChatFormatting
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.tags.TagKey
import net.minecraft.util.Mth
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.reflect.full.memberProperties

@Suppress("NullableBooleanElvis")
class AbyssalContract(
    id: UUID,
    targetItems: List<Item>,
    targetTags: List<TagKey<Item>>,
    targetBlockTags: List<TagKey<Block>>,
    targetConditions: List<ItemCondition>,

    startTime: Long,
    var currentCycleStart: Long,
    val cycleDurationMs: Long,

    countPerUnit: Int,
    val baseUnitsDemanded: Int,
    var unitsFulfilled: Int,
    unitsFulfilledEver: Long,

    author: String,
    name: String?,
    description: String?,
    shortTargetList: String?,
    rarity: Int?,
    displayItem: ItemStack?,

    val reward: ItemStack,

    var level: Int,
    val quantityGrowthFactor: Double,
    val maxLevel: Int,

    var isActive: Boolean
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
    description,
    shortTargetList,
    rarity,
    displayItem
) {
    override val item: Item get() = ModItemRegistry.ABYSSAL_CONTRACT.get()
    override val displayName: MutableComponent
        get() {
            val rarityString = Component.translatable("${WingsContractsMod.MOD_ID}.rarity.${getRarity()}").string
            val nameString = Component.translatable(name ?: targetName).string
            val numeralString = Component.translatable("enchantment.level.$level").string

            return Component.translatable(
                "item.${WingsContractsMod.MOD_ID}.contract.abyssal",
                rarityString,
                nameString,
                numeralString
            )
        }

    override fun getBasicInfo(list: MutableList<Component>?): MutableList<Component> {
        val components = list ?: mutableListOf()

        val rewardsComponent = translateContract(
            "abyssal.rewards",
            formatReward(reward.count),
            countPerUnit,
        ).withStyle(ChatFormatting.DARK_PURPLE)

        val targetsList = getTargetListComponents(displayShort = false)
        if (targetsList.size <= 2) {
            components.add(targetsList.fold(rewardsComponent.append(CommonComponents.SPACE)) { acc, entry ->
                acc.append(entry.withStyle(ChatFormatting.DARK_PURPLE))
            })
        } else {
            components.add(
                rewardsComponent
                    .append(CommonComponents.SPACE)
                    .append(targetsList[0].withStyle(ChatFormatting.DARK_PURPLE))
            )
            components.addAll(targetsList.drop(1).map { it.withStyle(ChatFormatting.DARK_PURPLE) })
        }

        components.add(
            translateContract(
                "abyssal.max_reward_cycle",
                formatReward(unitsDemanded * reward.count),
            ).withStyle(ChatFormatting.LIGHT_PURPLE)
        )

        components.add(
            translateContract(
                "abyssal.max_reward",
                if (maxLevel <= 0) "∞" else maxLevel.toString(),
                formatReward(maxPossibleReward)
            ).withStyle(ChatFormatting.DARK_PURPLE)
        )

        components.add(
            translateContract(
                "units_fulfilled",
                unitsFulfilled,
                unitsDemanded,
                unitsFulfilled * countPerUnit,
                unitsDemanded * countPerUnit
            ).withStyle(ChatFormatting.LIGHT_PURPLE)
        )

        return super.getBasicInfo(components)
    }

    override fun getShortInfo(): Component = translateContract(
        "abyssal.short",
        countPerUnit,
        getTargetListComponents(displayShort = true).joinToString("|") { it.string },
        formatReward(reward.count),
        unitsFulfilled,
        unitsDemanded
    ).withStyle(ChatFormatting.DARK_PURPLE)

    fun getCycleInfo(): MutableList<Component> {
        val components = mutableListOf<Component>()
        val nextCycleStart = currentCycleStart + cycleDurationMs
        val timeRemaining = nextCycleStart - System.currentTimeMillis()
        val timeRemainingString = DenominationsHelper.denominateDurationToString(timeRemaining)

        val timeRemainingColor = getTimeRemainingColor(timeRemaining)

        if (Date(nextCycleStart) <= Date()) {
            components.add(translateContract("cycle_complete").withStyle(ChatFormatting.DARK_PURPLE))
        } else {
            components.add(translateContract("cycle_remaining").withStyle(timeRemainingColor))
            components.add(Component.literal("  $timeRemainingString").withStyle(timeRemainingColor))
        }

        return components
    }

    val unitsDemanded: Int get() = unitsDemandedAtLevel(level)

    fun unitsDemandedAtLevel(level: Int): Int {
        if (countPerUnit == 0) {
            return 0
        }

        return when (val growthFn = ModConfig.SERVER.contractGrowthFunction.get()) {
            GrowthFunctionOptions.LINEAR -> {
                val growth = (baseUnitsDemanded * (level - 1) * (quantityGrowthFactor - 1)).toInt()
                baseUnitsDemanded + growth
            }
            GrowthFunctionOptions.EXPONENTIAL -> (baseUnitsDemanded * quantityGrowthFactor.pow(level - 1)).toInt()
            else -> throw Error("Unrecognized contract growth function: $growthFn")
        }
    }

    override fun tryUpdateTick(tag: ContractTag?): Boolean {
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

    override fun onContractFulfilled(tag: ContractTag?) {
        super.onContractFulfilled(tag)
        if (level < maxLevel) {
            level += 1
            tag?.level = level
        }
    }

    override fun countConsumableUnits(items: NonNullList<ItemStack>): Int =
        min(super.countConsumableUnits(items), unitsDemanded - unitsFulfilled)

    override fun tryConsumeFromItems(tag: ContractTag, portal: ContractPortalBlockEntity): List<ItemStack> {
        val unitCount = countConsumableUnits(portal.cachedInput.items)
        if (unitCount == 0) {
            return listOf()
        }

        consumeUnits(unitCount, portal)

        unitsFulfilledEver += unitCount
        tag.unitsFulfilledEver = unitsFulfilledEver

        unitsFulfilled += unitCount
        tag.unitsFulfilled = unitsFulfilled

        return getRewardsForUnits(unitCount)
    }

    fun formatReward(count: Int): String {
        val rewardEntry = AvailableContractsManager.defaultRewards.firstOrNull { it.item.item == reward.item }
        return if (rewardEntry == null || rewardEntry.formatString == null) {
            "$count ${reward.displayName.string.trimBrackets()}"
        } else {
            String.format(rewardEntry.formatString, count)
        }
    }

    fun getRewardsForUnits(units: Int): List<ItemStack> {
        val rewardsList = (1..Mth.floor(reward.count * units.toDouble() / reward.maxStackSize)).map {
            reward.copyWithCount(reward.maxStackSize)
        }.toMutableList()

        val remainder = reward.count * units % reward.maxStackSize
        if (remainder != 0) {
            rewardsList.add(reward.copyWithCount(remainder))
        }

        return rewardsList
    }

    override fun reset(tag: ContractTag?, newCycleStart: Long) {
        if (unitsFulfilled >= unitsDemanded) {
            onContractFulfilled(tag)
        }

        currentCycleStart = newCycleStart
        tag?.currentCycleStart = currentCycleStart
        unitsFulfilled = 0
        tag?.unitsFulfilled = unitsFulfilled
    }

    val maxPossibleReward: Int
        get() {
            if (maxLevel <= 0) {
                val compare = unitsDemandedAtLevel(1).compareTo(unitsDemandedAtLevel(2))
                return when {
                    compare < 0 -> Int.MAX_VALUE
                    compare == 0 -> unitsDemandedAtLevel(1) * reward.count
                    else -> reward.count
                }
            }
            val maxUnitsDemanded = max(unitsDemandedAtLevel(1), unitsDemandedAtLevel(maxLevel))
            return maxUnitsDemanded * reward.count
        }

    override fun getRarity(): Int {
        if (rarity != null) {
            return rarity
        }

        val rewardEntry = AvailableContractsManager.defaultRewards.firstOrNull { it.item.item == reward.item }
        if (rewardEntry == null) {
            return 0
        }

        return AvailableContractsData.fakeData.rarityThresholds.indexOfLast { maxPossibleReward * rewardEntry.value > it } + 1
    }

    override fun save(nbt: CompoundTag?): ContractTag {
        val tag = super.save(nbt)

        tag.currentCycleStart = currentCycleStart
        tag.cycleDurationMs = cycleDurationMs
        tag.baseUnitsDemanded = baseUnitsDemanded
        tag.unitsFulfilled = unitsFulfilled
        tag.reward = Reward.Defined(reward)
        tag.level = level
        tag.quantityGrowthFactor = quantityGrowthFactor
        tag.maxLevel = maxLevel
        tag.isActive = isActive

        return tag
    }

    override val details
        get() = AbyssalContract::class.memberProperties
            .filter { it.name != "details" }
            .associate { prop ->
                return@associate Pair(
                    prop.name, when (prop.name) {
                        "targetItems" -> targetItems.map { it.defaultInstance.details }
                        "targetTags" -> targetTags.map { "#${it.location}" }
                        "targetBlockTags" -> targetBlockTags.map { "#${it.location}" }
                        "reward" -> reward.details
                        else -> prop.get(this)
                    })
            }.toMutableMap()

    val isValid
        get() = reward.item != Items.AIR
                && (targetItems.any { it != Items.AIR }
                || targetTags.any()
                || targetBlockTags.any()
                || targetConditions.any())

    companion object {
        var (ContractTag).reward by reward()

        var (ContractTag).level by int()
        var (ContractTag).quantityGrowthFactor by double()
        var (ContractTag).maxLevel by int()

        var (ContractTag).isActive by boolean()

        var (ContractTag).currentCycleStart by long()
        var (ContractTag).cycleDurationMs by long()
        var (ContractTag).baseUnitsDemanded by int()
        var (ContractTag).unitsFulfilled by int()

        fun load(contract: ContractTag, data: AvailableContractsData? = null): AbyssalContract {
            val reward = contract.reward ?: Reward.Random(1.0)
            return AbyssalContract(
                id = contract.id ?: UUID.randomUUID(),
                targetItems = contract.targetItems ?: listOf(),
                targetTags = contract.targetTags ?: listOf(),
                targetBlockTags = contract.targetBlockTags ?: listOf(),
                targetConditions = contract.targetConditions ?: listOf(),
                startTime = contract.startTime ?: System.currentTimeMillis(),
                currentCycleStart = contract.currentCycleStart ?: System.currentTimeMillis(),
                cycleDurationMs = contract.cycleDurationMs ?: ModConfig.SERVER.defaultCycleDurationMs.get(),
                countPerUnit = contract.countPerUnit ?: 64,
                baseUnitsDemanded = contract.baseUnitsDemanded ?: 64,
                unitsFulfilled = contract.unitsFulfilled ?: 0,
                unitsFulfilledEver = contract.unitsFulfilledEver ?: 0,
                author = contract.author ?: ModConfig.SERVER.defaultAuthor.get(),
                name = contract.name,
                description = contract.description,
                shortTargetList = contract.shortTargetList,
                rarity = contract.rarity,
                displayItem = contract.displayItem,
                reward = when (reward) {
                    is Reward.Defined -> reward.itemStack
                    is Reward.Random ->
                        data?.getRandomReward(reward.value) ?: AvailableContractsData.FALLBACK_REWARD.item
                },
                level = contract.level ?: 1,
                quantityGrowthFactor = contract.quantityGrowthFactor ?: ModConfig.SERVER.defaultGrowthFactor.get(),
                maxLevel = contract.maxLevel ?: ModConfig.SERVER.defaultMaxLevel.get(),
                isActive = contract.isActive ?: true
            )
        }
    }
}