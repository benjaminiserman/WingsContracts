@file:OptIn(ExperimentalStdlibApi::class)

package dev.biserman.wingscontracts.nbt

import dev.architectury.registry.fuel.FuelRegistry
import dev.biserman.wingscontracts.WingsContractsMod
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack

class ItemCondition(val text: String, val match: (ItemStack) -> Boolean) {
    override fun toString() = text
}

object ItemConditionParser {
    val attributeOperationsMap = mapOf(
        "add" to AttributeModifier.Operation.ADDITION,
        "multBase" to AttributeModifier.Operation.MULTIPLY_BASE,
        "multTotal" to AttributeModifier.Operation.MULTIPLY_TOTAL
    )

    fun parseEntries(text: String): List<String> {
        val entries = mutableListOf<String>()
        var inQuotes = false
        var currentEntry = ""
        for (i in 0..<text.length) {
            if (text[i] == '\'') {
                if (i == 0 || text[i - 1] != '\\') {
                    inQuotes = !inQuotes
                }
            }

            if (text[i] == ',') {
                if (!inQuotes) {
                    entries.add(currentEntry)
                    currentEntry = ""
                    continue
                }
            }

            currentEntry += text[i]
        }

        if (currentEntry.isNotEmpty()) {
            entries.add(currentEntry)
        }

        return entries.toList()
    }

    val navigationRegex = Regex("""(?!\\)\.""")

    // navigate recursively down a path of CompoundTags and convert the end value into a String
    private fun navigate(
        keyComponents: List<String>,
        default: String,
        soFar: (CompoundTag?) -> CompoundTag? = { it }
    ): (CompoundTag?) -> String {
        if (keyComponents.size == 1) {
            return { soFar(it)?.get(keyComponents[0])?.toString() ?: default }
        }

        return navigate(keyComponents.drop(1), default) { it?.getCompound(keyComponents[0]) }
    }

    private fun (ItemStack).getAttributeValue(attribute: Attribute, operation: AttributeModifier.Operation): Double {
        val item = this.item
        val equipmentSlot = when {
            item is ArmorItem -> item.equipmentSlot
            else -> EquipmentSlot.MAINHAND
        }

        val relevantModifiers = this
            .getAttributeModifiers(equipmentSlot)
            .get(attribute)
            .filter { it.operation == operation }
            .map { it.amount }

        return if (operation == AttributeModifier.Operation.ADDITION) {
            relevantModifiers.sum()
        } else {
            relevantModifiers.reduce(Double::times)
        }
    }

    val conditionRegex = Regex("^(.+?)(!<-|==|!=|<=|>=|<-|!\\$|<|>|\\$)(.+)$")
    fun parseCondition(condition: String): ItemCondition? {
        val match = conditionRegex.matchEntire(condition)
        if (match == null) {
            WingsContractsMod.LOGGER.warn("Failed to parse condition $condition")
            return null
        }
        val key = match.groups[1]?.value?.trim() ?: return null
        val operator = match.groups[2]?.value ?: return null
        val value = match.groups[3]?.value?.trim() ?: return null

        val keyComponents = key.split(navigationRegex)
        val navigationComponents = keyComponents.drop(1)

        fun wrapNavigate(
            default: String
        ): (ItemStack) -> String = ({ navigate(navigationComponents, default)(it.tag) })

        val fetchItemValue: (ItemStack) -> String = when (keyComponents[0]) {
            "tag" -> when {
                value == "true" || value == "false" -> wrapNavigate("false")
                value.toIntOrNull() != null -> wrapNavigate("0")
                value.toDoubleOrNull() != null -> wrapNavigate("0.0")
                else -> wrapNavigate("")
            }
            "attribute" -> attribute@ ({
                it.getAttributeValue(
                    BuiltInRegistries.ATTRIBUTE.get(
                        ResourceLocation.tryParse(
                            keyComponents.subList(
                                1,
                                keyComponents.size - 1
                            ).joinToString(".")
                        )
                    ) ?: return@attribute "0",
                    attributeOperationsMap[keyComponents[keyComponents.size - 1]] ?: return@attribute "0"
                ).toString()
            })
            "mod" -> ({ it.item.`arch$registryName`()?.namespace ?: "" })
            "path" -> ({ it.item.`arch$registryName`()?.path ?: "" })
            "id" -> ({ it.item.`arch$registryName`()?.toString() ?: "" })
            "isBarVisible" -> ({ it.isBarVisible.toString() })
            "barWidth" -> ({ it.barWidth.toString() })
            "barColor" -> ({ it.barColor.toString() })
            "hasFoil" -> ({ it.hasFoil().toString() })
            "rarity" -> ({ it.rarity.toString() })
            "isDamageable" -> ({ it.isDamageableItem.toString() })
            "durabilityPercent" -> ({ (it.damageValue.toDouble() / it.maxDamage.toDouble()).toString() })
            "isEnchantable" -> ({ it.isEnchantable.toString() })
            "isEnchanted" -> ({ it.isEnchanted.toString() })
            "isFireResistant" -> ({ it.item.isFireResistant.toString() })
            "isEdible" -> ({ it.item.isEdible.toString() })
            "maxStackSize" -> ({ it.item.maxStackSize.toString() })
            "maxDamage" -> ({ it.item.maxDamage.toString() })
            "nutrition" -> ({ it.item.foodProperties?.nutrition?.toString() ?: "0" })
            "saturationModifier" -> ({ it.item.foodProperties?.saturationModifier?.toString() ?: "0.0" })
            "isMeat" -> ({ it.item.foodProperties?.isMeat?.toString() ?: "false" })
            "canAlwaysEat" -> ({ it.item.foodProperties?.canAlwaysEat()?.toString() ?: "false" })
            "isFastFood" -> ({ it.item.foodProperties?.isFastFood?.toString() ?: "false" })
            "isBlock" -> ({ (it.item is BlockItem).toString() })
            "class" -> ({ it.item.javaClass.name })
            "displayName" -> ({ it.displayName.string })
            "burnTicks" -> ({ FuelRegistry.get(it).toString() })
            else -> throw Error("Condition key not recognized: ${keyComponents[0]}")
        }

        val compareToValue: String.() -> Int = when {
            value == "true" || value == "false" -> ({ this.compareTo(value) })
            value.toDoubleOrNull() != null -> ({ this.toDouble().compareTo(value.toDouble()) })
            else -> ({ this.trim('"').compareTo(value.trim('"', '\'')) })
        }

        return when (operator) {
            "==" -> ItemCondition(condition) { fetchItemValue(it).compareToValue() == 0 }
            "!=" -> ItemCondition(condition) { fetchItemValue(it).compareToValue() != 0 }
            "<" -> ItemCondition(condition) { fetchItemValue(it).compareToValue() < 0 }
            ">" -> ItemCondition(condition) { fetchItemValue(it).compareToValue() > 0 }
            "<=" -> ItemCondition(condition) { fetchItemValue(it).compareToValue() <= 0 }
            ">=" -> ItemCondition(condition) { fetchItemValue(it).compareToValue() >= 0 }
            "<-" -> ItemCondition(condition) { fetchItemValue(it).contains(value) }
            "!<-" -> ItemCondition(condition) { !fetchItemValue(it).contains(value) }
            "$" -> {
                val regex = Regex(value)
                ItemCondition(condition) { regex.matches(fetchItemValue(it)) }
            }
            "!$" -> {
                val regex = Regex(value)
                ItemCondition(condition) { !regex.matches(fetchItemValue(it)) }
            }
            else -> {
                WingsContractsMod.LOGGER.warn("Failed to parse condition $condition: unknown operator $operator")
                null
            }
        }
    }

    fun parse(text: String): List<ItemCondition> {
        val entries = parseEntries(text)
        return entries.mapNotNull { parseCondition(it) }
    }
}