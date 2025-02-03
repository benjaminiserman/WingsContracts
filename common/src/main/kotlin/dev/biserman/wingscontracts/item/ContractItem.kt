package dev.biserman.wingscontracts.item

import dev.biserman.wingscontracts.registry.ItemRegistry
import dev.biserman.wingscontracts.tag.ContractTag
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import java.util.*
import kotlin.math.min

class ContractItem(properties: Properties) : Item(properties) {
    // TODO: how do I localize this properly?
    // e.g.: Contract de Niveau 10 des Diamants de winggar
    override fun getName(itemStack: ItemStack): Component {
        val contractTag = ContractTag(getBaseTag(itemStack) ?: return Component.literal("Unknown Contract"))

        val author = contractTag.author.get()
        val level = contractTag.level.get()
        val targetItem = contractTag.targetItem.get()
        val targetTag = contractTag.targetTag.get()

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
            val targetItemDisplayName = Component
                .translatable(
                    BuiltInRegistries.ITEM[ResourceLocation.tryParse(
                        targetItem
                    )].descriptionId
                )
                .string
            stringBuilder.append("$targetItemDisplayName ")
        }

        if (targetTag != null) {
            val split = targetTag.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
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
        components.add(Component.literal(""))
        if (Screen.hasShiftDown()) {
            components.add(Component.literal(""))
        }
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
                contractTag.targetItem.put(targetItem)
            }

            if (targetTag != null) {
                contractTag.targetTag.put(targetTag)
            }

            contractTag.rewardItem.put(rewardItem)
            contractTag.unitPrice.put(unitPrice)
            contractTag.countPerUnit.put(countPerUnit)
            contractTag.levelOneQuantity.put(levelOneQuantity)
            contractTag.quantityGrowthFactor.put(quantityGrowthFactor)
            contractTag.startLevel.put(startLevel)
            contractTag.level.put(startLevel)
            contractTag.startTime.put(System.currentTimeMillis())
            contractTag.currentCycleStart.put(System.currentTimeMillis())
            contractTag.cycleDurationMs.put(1000L * 60 * 60 * 24 * 7)
            contractTag.quantityFulfilled.put(0)
            contractTag.maxLevel.put(maxLevel)
            contractTag.author.put(author)

            val contractItem = ItemRegistry.CONTRACT.get() ?: return ItemStack.EMPTY
            val itemStack = ItemStack(contractItem)
            itemStack.addTagElement(ContractTag.CONTRACT_INFO, contractTag.tag)
            return itemStack
        }

        fun getBaseTag(contract: ItemStack): CompoundTag? {
            return contract.getTagElement(ContractTag.CONTRACT_INFO)
        }

        fun getQuantityDemanded(
            levelOneQuantity: Int, startLevel: Int, quantityGrowthFactor: Float,
            countPerUnit: Int
        ): Int {
            val quantity = levelOneQuantity + (levelOneQuantity * (startLevel - 1) * quantityGrowthFactor).toInt()
            return quantity - quantity % countPerUnit
        }

        fun ContractTag.getQuantityDemanded() = getQuantityDemanded(
            this.levelOneQuantity.get(),
            this.startLevel.get(),
            this.quantityGrowthFactor.get(),
            this.countPerUnit.get()
        )

        fun tick(contract: ItemStack) {
            val currentTime = System.currentTimeMillis()
            val contractTag = ContractTag(getBaseTag(contract) ?: return)

            val currentCycleStart = contractTag.currentCycleStart.get() ?: return
            val contractPeriod = contractTag.cycleDurationMs.get()
            val cyclesPassed = ((currentTime - currentCycleStart) / contractPeriod).toInt()
            if (cyclesPassed > 0) {
                update(contract, cyclesPassed)
            }
        }

        private fun update(contract: ItemStack, cycles: Int) {
            val contractTag = ContractTag(getBaseTag(contract) ?: return)
            val currentCycleStart = contractTag.currentCycleStart.get() ?: return

            val quantityDemanded = contractTag.getQuantityDemanded()
            val quantityFulfilled = contractTag.quantityFulfilled.get()
            if (quantityFulfilled >= quantityDemanded) {
                val currentLevel = contractTag.level.get()
                val maxLevel = contractTag.maxLevel.get()

                if (currentLevel < maxLevel) {
                    contractTag.level.put(currentLevel + 1)
                }
            }

            contractTag.currentCycleStart.put(currentCycleStart + contractTag.cycleDurationMs.get() * cycles)
            contractTag.quantityFulfilled.put(0)
        }

        fun matches(contract: ItemStack, itemStack: ItemStack): Boolean {
            val contractTag = ContractTag(getBaseTag(contract) ?: return false)
            val targetTag = contractTag.targetTag.get() ?: ""
            if (targetTag.isNotEmpty()) {
                val tagKey = TagKey.create(Registries.ITEM, ResourceLocation.tryParse(targetTag) ?: return false)
                return itemStack.`is`(tagKey)
            }

            val targetItem = contractTag.targetItem.get()
            return itemStack.item.descriptionId == targetItem
        }

        fun remainingQuantity(contract: ItemStack): Int {
            val contractTag = ContractTag(getBaseTag(contract) ?: return -1)
            val quantityDemanded = contractTag.getQuantityDemanded()
            val quantityFulfilled = contractTag.quantityFulfilled.get()

            return quantityDemanded - quantityFulfilled
        }

        fun targetItem(contract: ItemStack): ItemStack? {
            val contractTag = ContractTag(getBaseTag(contract) ?: return null)

            val targetItem = contractTag.targetItem.get() ?: return null
            return ItemStack(
                BuiltInRegistries.ITEM[ResourceLocation.tryParse(
                    targetItem
                )]
            )
        }

        fun consume(contract: ItemStack, itemStack: ItemStack): Int {
            val remainingQuantity = remainingQuantity(contract)
            if (remainingQuantity > 0 && matches(contract, itemStack)) {
                val amountConsumed = min(itemStack.count.toDouble(), remainingQuantity.toDouble()).toInt()
                itemStack.count -= amountConsumed
                val contractTag = ContractTag(getBaseTag(contract) ?: return 0)
                contractTag.quantityFulfilled.put(
                    contractTag.quantityFulfilled.get() + amountConsumed
                )
                itemStack.addTagElement(ContractTag.CONTRACT_INFO, contractTag.tag)
                return amountConsumed
            }

            return 0
        }
    }
}
