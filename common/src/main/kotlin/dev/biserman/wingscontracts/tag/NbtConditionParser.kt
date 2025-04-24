@file:OptIn(ExperimentalStdlibApi::class)

package dev.biserman.wingscontracts.tag

import dev.biserman.wingscontracts.WingsContractsMod
import net.minecraft.nbt.CompoundTag

class NbtCondition(val text: String, val match: (CompoundTag) -> Boolean) {
    override fun toString() = text
}

object NbtConditionParser {
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

    val conditionRegex = Regex("^(.+?)(==|!=|<=|>=|<|>)(.+)$")
    fun parseCondition(condition: String): NbtCondition? {
        val match = conditionRegex.matchEntire(condition)
        if (match == null) {
            WingsContractsMod.LOGGER.warn("Failed to parse condition $condition")
            return null
        }
        val key = match.groups[1]?.value?.trim() ?: return null
        val operator = match.groups[2]?.value ?: return null
        val value = match.groups[3]?.value?.trim() ?: return null

        val compareToValue: (CompoundTag) -> Int = when {
            value == "true" || value == "false" -> ({ it.getBoolean(key).toString().compareTo(value) })
            value.toIntOrNull() != null -> ({ it.getInt(key).compareTo(value.toInt()) })
            value.toDoubleOrNull() != null -> ({ it.getDouble(key).compareTo(value.toDouble()) })
            else -> ({ it.getString(key).compareTo(value.trim('\'')) })
        }

        return when (operator) {
            "==" -> NbtCondition(condition) { compareToValue(it) == 0 }
            "!=" -> NbtCondition(condition) { compareToValue(it) != 0 }
            "<" -> NbtCondition(condition) { compareToValue(it) < 0 }
            ">" -> NbtCondition(condition) { compareToValue(it) > 0 }
            "<=" -> NbtCondition(condition) { compareToValue(it) <= 0 }
            ">=" -> NbtCondition(condition) { compareToValue(it) <= 0 }
            else -> {
                WingsContractsMod.LOGGER.warn("Failed to parse condition $condition: unknown operator $operator")
                null
            }
        }
    }

    fun parse(text: String): List<NbtCondition> {
        val entries = parseEntries(text)
        return entries.mapNotNull { parseCondition(it) }
    }
}