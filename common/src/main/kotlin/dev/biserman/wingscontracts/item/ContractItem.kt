package dev.biserman.wingscontracts.item

import dev.biserman.wingscontracts.registry.ItemRegistry
import dev.biserman.wingscontracts.tag.ContractTag
import dev.biserman.wingscontracts.util.DenominationHelper
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import java.util.*
import kotlin.contracts.contract
import kotlin.math.ceil
import kotlin.math.min

class ContractItem(properties: Properties) : Item(properties) {
    // TODO: how do I localize this properly?
    // e.g.: Contract de Niveau 10 des Diamants de winggar
    override fun getName(itemStack: ItemStack): Component {
        val contractTag = getBaseTag(itemStack) ?: return Component.literal("Unknown Contract")

        val author = contractTag.author.get()
        val level = contractTag.level.get()
        val targetItem = contractTag.targetItem
        val targetTagKey = contractTag.targetTagKey.get()

        val stringBuilder = StringBuilder()
        if (author != null) {
            stringBuilder.append("$author's ")
        }

        stringBuilder.append(
            if (level == -1) {
                "Endless "
            } else {
                "Level $level "
            }
        )

        if (targetItem != null) {
            val targetItemDisplayName = Component.translatable(targetItem.descriptionId).string
            stringBuilder.append("$targetItemDisplayName ")
        }

        if (targetTagKey != null) {
            val split = targetTagKey.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (split.size >= 2) {
                val tagName = split[1]
                stringBuilder.append(tagName.substring(0, 1).uppercase(Locale.getDefault()))
                stringBuilder.append(tagName.substring(1))
                stringBuilder.append(" ")
            }
        }

        stringBuilder.append(Component.translatable(descriptionId).string)

        return Component.literal(stringBuilder.toString())
    }

    override fun appendHoverText(
        itemStack: ItemStack,
        level: Level?,
        components: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        val contractTag = getBaseTag(itemStack) ?: return
        val rewardItem = contractTag.rewardItem ?: return
        val targetItem = contractTag.targetItem
        val targetTagKey = contractTag.targetTagKey.get()
        val rewardName = rewardItem.getName(ItemStack(rewardItem)).string
        val currentCycleStart = contractTag.currentCycleStart.get() ?: return

        components.add(Component.literal("Quantity Fulfilled: ${contractTag.quantityFulfilled.get()}/${contractTag.quantityDemanded}"))

        if (!targetTagKey.isNullOrEmpty()) {
            components.add(Component.literal("Rewards ${contractTag.unitPrice.get()} $rewardName for every ${contractTag.countPerUnit.get()} items matching $targetTagKey"))
        } else if (targetItem != null) {
            val targetName = targetItem.getName(ItemStack(targetItem)).string
            components.add(Component.literal("Rewards ${contractTag.unitPrice.get()} $rewardName for every ${contractTag.countPerUnit.get()} $targetName"))
        }

        val nextCycleStart = currentCycleStart + contractTag.cycleDurationMs.get()
        val timeRemaining = DenominationHelper
            .denominate(nextCycleStart - System.currentTimeMillis(), DenominationHelper.timeDenominationsWithoutMs)
            .joinToString(separator = ", ") { kvp ->
                "${kvp.second} ${kvp.first}${
                    if (kvp.second == 1) {
                        ""
                    } else {
                        "s"
                    }
                }"
            }
        components.add(Component.literal("Current Cycle Ends at ${Date(nextCycleStart)}"))
        if (Date(nextCycleStart) <= Date()) {
            components.add(Component.literal("Cycle Complete!"))
            components.add(Component.literal("Right-click with contract in hand or place in contract portal to start next cycle."))
        } else {
            components.add(Component.literal("Current Cycle Remaining Time:"))
            components.add(Component.literal("    $timeRemaining"))
        }

        if (Screen.hasShiftDown()) {
            components.add(Component.literal("Max Level: ${contractTag.maxLevel.get()}"))
            components.add(Component.literal("Base Quantity: ${contractTag.levelOneQuantity.get()}"))
            components.add(Component.literal("Quantity Growth Factor: ${contractTag.quantityGrowthFactor.get()}"))
            val startTime = contractTag.startTime.get()
            components.add(
                Component.literal(
                    "Started On: ${
                        if (startTime == null) {
                            "???"
                        } else Date(startTime)
                    }"
                )
            )
        } else {
            components.add(Component.literal("Hold shift for more info"))
        }
    }

    override fun use(
        level: Level,
        player: Player,
        interactionHand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        val itemInHand = player.getItemInHand(interactionHand)
        if (itemInHand.item is ContractItem) {
            tick(itemInHand)
        }

        return super.use(level, player, interactionHand)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        @JvmStatic
        fun createContract(
            targetItem: String?,
            targetTag: String?,
            rewardItem: String,
            unitPrice: Int,
            countPerUnit: Int,
            levelOneQuantity: Int,
            quantityGrowthFactor: Float,
            startLevel: Int,
            maxLevel: Int,
            author: String?
        ): ItemStack {
            val contractTag = ContractTag(CompoundTag())

            if (targetItem != null) {
                contractTag.targetItemKey.put(targetItem)
            }

            if (targetTag != null) {
                contractTag.targetTagKey.put(targetTag)
            }

            contractTag.rewardItemKey.put(rewardItem)
            contractTag.unitPrice.put(unitPrice)
            contractTag.countPerUnit.put(countPerUnit)
            contractTag.levelOneQuantity.put(levelOneQuantity)
            contractTag.quantityGrowthFactor.put(quantityGrowthFactor)
            contractTag.startLevel.put(startLevel)
            contractTag.level.put(startLevel)
            contractTag.startTime.put(System.currentTimeMillis())
            contractTag.currentCycleStart.put(System.currentTimeMillis())
//            contractTag.cycleDurationMs.put(1000L * 60 * 60 * 24 * 7)
            contractTag.cycleDurationMs.put(1000L * 60 * 5) // 5-minute cycles for now
            contractTag.quantityFulfilled.put(0)
            contractTag.quantityFulfilledEver.put(0)
            contractTag.maxLevel.put(maxLevel)
            contractTag.author.put(author)

            val contractItem = ItemRegistry.CONTRACT.get() ?: return ItemStack.EMPTY
            val itemStack = ItemStack(contractItem)
            itemStack.addTagElement(ContractTag.CONTRACT_INFO, contractTag.tag)
            return itemStack
        }

        fun getBaseTag(contract: ItemStack): ContractTag? {
            return ContractTag(contract.getTagElement(ContractTag.CONTRACT_INFO) ?: return null)
        }

        fun tick(contract: ItemStack): Boolean {
            val currentTime = System.currentTimeMillis()
            val contractTag = getBaseTag(contract) ?: return false

            val currentCycleStart = contractTag.currentCycleStart.get() ?: return false
            val contractPeriod = contractTag.cycleDurationMs.get()
            val cyclesPassed = ((currentTime - currentCycleStart) / contractPeriod).toInt()
            if (cyclesPassed > 0) {
                return update(contract, cyclesPassed)
            }

            return false
        }

        private fun update(contract: ItemStack, cycles: Int): Boolean {
            val contractTag = getBaseTag(contract) ?: return false
            val currentCycleStart = contractTag.currentCycleStart.get() ?: return false

            val quantityFulfilled = contractTag.quantityFulfilled.get()
            if (quantityFulfilled >= contractTag.quantityDemanded) {
                val currentLevel = contractTag.level.get()
                val maxLevel = contractTag.maxLevel.get()

                if (currentLevel < maxLevel) {
                    contractTag.level.put(currentLevel + 1)
                }
            }

            contractTag.currentCycleStart.put(currentCycleStart + contractTag.cycleDurationMs.get() * cycles)
            contractTag.quantityFulfilled.put(0)

            return true
        }

        fun matches(contract: ItemStack, itemStack: ItemStack): Boolean {
            val contractTag = getBaseTag(contract) ?: return false

            val targetTag = contractTag.targetTag
            if (targetTag != null) {
                return itemStack.`is`(targetTag)
            }

            return itemStack.item.`arch$registryName`().toString() == contractTag.targetItemKey.get()
        }

        fun remainingQuantity(contract: ItemStack): Int {
            val contractTag = getBaseTag(contract) ?: return -1
            val quantityFulfilled = contractTag.quantityFulfilled.get()

            return contractTag.quantityDemanded - quantityFulfilled
        }


        fun consume(contract: ItemStack, itemStack: ItemStack): Int {
            val remainingQuantity = remainingQuantity(contract)
            val contractTag = getBaseTag(contract) ?: return 0
            val countPerUnit = contractTag.countPerUnit.get()

            if (matches(contract, itemStack) && remainingQuantity >= countPerUnit) {
                val countWithoutRemainder = (itemStack.count / countPerUnit) * countPerUnit
                val amountConsumed = min(countWithoutRemainder, remainingQuantity)
                itemStack.shrink(amountConsumed)
                contractTag.quantityFulfilled.put(
                    contractTag.quantityFulfilled.get() + amountConsumed
                )
                contractTag.quantityFulfilledEver.put(
                    contractTag.quantityFulfilledEver.get() + amountConsumed
                )
                itemStack.addTagElement(ContractTag.CONTRACT_INFO, contractTag.tag)

                val unitsConsumed = ceil(amountConsumed.toDouble() / countPerUnit).toInt()
                val rewardsReceived = unitsConsumed * contractTag.unitPrice.get()
                return rewardsReceived
            }

            return 0
        }
    }
}
