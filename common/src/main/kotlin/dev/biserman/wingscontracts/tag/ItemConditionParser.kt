@file:OptIn(ExperimentalStdlibApi::class)

package dev.biserman.wingscontracts.tag

import dev.biserman.wingscontracts.WingsContractsMod
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack

class ItemCondition(val text: String, val match: (ItemStack) -> Boolean) {
    override fun toString() = text
}

object ItemConditionParser {
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

        if (entries.size > 1) {
            WingsContractsMod.LOGGER.info("Found big one! ${entries.joinToString(", ")}")
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

    val conditionRegex = Regex("^(.+?)(==|!=|<=|>=|<-|<|>)(.+)$")
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

        val fetchValue: (ItemStack) -> String = when (keyComponents[0]) {
            "tag" -> when {
                value == "true" || value == "false" -> wrapNavigate("false")
                value.toIntOrNull() != null -> wrapNavigate("0")
                value.toDoubleOrNull() != null -> wrapNavigate("0.0")
                else -> wrapNavigate("")
            }
            "mod" -> ({ it.item.`arch$registryName`()?.namespace ?: "" })
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
            "nutrition" -> ({ it.item.foodProperties?.nutrition?.toString() ?: "0"})
            "saturationModifier" -> ({ it.item.foodProperties?.saturationModifier?.toString() ?: "0.0"})
            "isMeat" -> ({ it.item.foodProperties?.isMeat?.toString() ?: "false"})
            "canAlwaysEat" -> ({ it.item.foodProperties?.canAlwaysEat()?.toString() ?: "false"})
            "fastFood" -> ({ it.item.foodProperties?.isFastFood?.toString() ?: "false"})
            else -> throw Error("Condition key not recognized: ${keyComponents[0]}")
        }

        val compareToValue: String.() -> Int = when {
            value == "true" || value == "false" -> ({ this.compareTo(value) })
            value.toIntOrNull() != null -> ({ this.toInt().compareTo(value.toInt()) })
            value.toDoubleOrNull() != null -> ({ this.toDouble().compareTo(value.toDouble()) })
            else -> ({ this.trim('"').compareTo(value.trim('"', '\'')) })
        }

        return when (operator) {
            "==" -> ItemCondition(condition) { fetchValue(it).compareToValue() == 0 }
            "!=" -> ItemCondition(condition) { fetchValue(it).compareToValue() != 0 }
            "<" -> ItemCondition(condition) { fetchValue(it).compareToValue() < 0 }
            ">" -> ItemCondition(condition) { fetchValue(it).compareToValue() > 0 }
            "<=" -> ItemCondition(condition) { fetchValue(it).compareToValue() <= 0 }
            ">=" -> ItemCondition(condition) { fetchValue(it).compareToValue() >= 0 }
            "<-" -> ItemCondition(condition) { fetchValue(it).contains(value) }
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