package dev.biserman.wingscontracts.util

import kotlin.math.floor

object DenominationHelper {
    val timeDenominations = mapOf(
        "Millisecond" to 1L,
        "Second" to 1000L,
        "Minute" to 1000L * 60,
        "Hour" to 1000L * 60 * 60,
        "Day" to 1000L * 60 * 60 * 24
    )

    val timeDenominationsWithoutMs = timeDenominations.filterKeys { x -> x != "Millisecond" }

    fun denominate(value: Double, denominations: Map<String, Double>): List<Pair<String, Int>> {
        if (denominations.values.any { x -> x <= 0 }) {
            throw IllegalArgumentException("all denominations must be positive")
        }

        val results = mutableListOf<Pair<String, Int>>()
        var runningValue = value
        while (true) {
            val denomination =
                denominations.filter { x -> x.value <= runningValue }.maxByOrNull { x -> x.value } ?: break
            val unitsToTake = floor(runningValue / denomination.value).toInt()
            runningValue -= unitsToTake * denomination.value
            results.add(Pair(denomination.key, unitsToTake))
        }

        return results.toList()
    }

    fun denominate(value: Long, denominations: Map<String, Long>): List<Pair<String, Int>> {
        if (denominations.values.any { x -> x <= 0 }) {
            throw IllegalArgumentException("all denominations must be positive")
        }

        val results = mutableListOf<Pair<String, Int>>()
        var runningValue = value
        while (true) {
            val denomination =
                denominations.filter { x -> x.value <= runningValue }.maxByOrNull { x -> x.value } ?: break
            val unitsToTake = (runningValue / denomination.value).toInt()
            runningValue -= unitsToTake * denomination.value
            results.add(Pair(denomination.key, unitsToTake))
        }

        return results.toList()
    }
}