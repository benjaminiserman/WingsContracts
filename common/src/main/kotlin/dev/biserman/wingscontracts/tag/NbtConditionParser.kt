@file:OptIn(ExperimentalStdlibApi::class)

package dev.biserman.wingscontracts.tag

import net.minecraft.nbt.CompoundTag

class NbtCondition(val text: String, val match: (CompoundTag) -> Boolean)

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

            if (text[i] == ';') {
                if (inQuotes) {
                    entries.add(currentEntry)
                    currentEntry = ""
                    continue
                }
            }

            currentEntry += text[i]
        }

        return entries.toList()
    }

    val conditionRegex = Regex("""^(\w+?)(==|!=|<=|>=|<|>)(.+)""")
    fun parseCondition(condition: String): NbtCondition? {
        val match = conditionRegex.matchEntire(condition) ?: return null
        val key = match.groups[0]?.value ?: return null
        val operator = match.groups[1]?.value ?: return null
        val value = match.groups[2]?.value ?: return null

        val compareToValue: (CompoundTag) -> Int = when {
            value[0] == '\'' && value[value.length - 1] == '\'' -> ({
                it.getString(key).compareTo(value.substring(1, value.length - 1))
            })
            value == "true" || value == "false" -> ({ it.getBoolean(key).toString().compareTo(value) })
            value.toIntOrNull() != null -> ({ it.getInt(key).compareTo(value.toInt()) })
            value.toDoubleOrNull() != null -> ({ it.getDouble(key).compareTo(value.toDouble()) })
            else -> return null
        }

        return when (operator) {
            "==" -> NbtCondition(condition) { compareToValue(it) == 0 }
            "!=" -> NbtCondition(condition) { compareToValue(it) != 0 }
            "<" -> NbtCondition(condition) { compareToValue(it) < 0 }
            ">" -> NbtCondition(condition) { compareToValue(it) > 0 }
            "<=" -> NbtCondition(condition) { compareToValue(it) <= 0 }
            ">=" -> NbtCondition(condition) { compareToValue(it) <= 0 }
            else -> null
        }
    }

    fun parse(text: String): List<NbtCondition> {
        val entries = parseEntries(text)
        return entries.mapNotNull { parseCondition(it) }
    }
}