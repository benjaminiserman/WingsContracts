package dev.biserman.wingscontracts.util

import dev.biserman.wingscontracts.WingsContractsMod
import net.minecraft.network.chat.Component
import kotlin.math.floor

object DenominationsHelper {
    fun translateTime(key: String): String = Component.translatable("${WingsContractsMod.MOD_ID}.time.$key").string

    @Suppress("MemberVisibilityCanBePrivate")
    val timeDenominations = mapOf(
        translateTime("ms") to 1L,
        translateTime("second") to 1000L,
        translateTime("minute") to 1000L * 60,
        translateTime("hour") to 1000L * 60 * 60,
        translateTime("day") to 1000L * 60 * 60 * 24
    )

    val timeDenominationsWithoutMs = timeDenominations.filterKeys { x -> x != "Millisecond" }

    fun denominateDurationToString(duration: Long) = denominate(
        duration, timeDenominationsWithoutMs
    ).asSequence().joinToString(separator = ", ") { kvp ->
        "${kvp.second} ${kvp.first}${
            if (kvp.second == 1) {
                ""
            } else {
                "s"
            }
        }"
    }

    fun <T> denominate(value: Double, denominations: Map<T, Double>) = iterator {
        if (denominations.values.any { x -> x <= 0 }) {
            throw IllegalArgumentException("all denominations must be positive")
        }

        var runningValue = value
        while (true) {
            val denomination =
                denominations.asSequence().filter { x -> x.value <= runningValue }.maxByOrNull { x -> x.value } ?: break
            val unitsToTake = floor(runningValue / denomination.value).toInt()
            runningValue -= unitsToTake * denomination.value
            yield(Pair(denomination.key, unitsToTake))
        }
    }

    fun <T> getLargestDenomination(value: Double, denominations: Map<T, Double>): Pair<T, Int>? {
        val denomination = denominations.filter { x -> x.value <= value }.maxByOrNull { x -> x.value } ?: return null
        val unitsToTake = (value / denomination.value).toInt()
        return Pair(denomination.key, unitsToTake)
    }

    fun <T> denominate(value: Long, denominations: Map<T, Long>) = iterator {
        if (denominations.values.any { x -> x <= 0 }) {
            throw IllegalArgumentException("all denominations must be positive")
        }

        var runningValue = value
        while (true) {
            val denomination =
                denominations.filter { x -> x.value <= runningValue }.maxByOrNull { x -> x.value } ?: break
            val unitsToTake = (runningValue / denomination.value).toInt()
            runningValue -= unitsToTake * denomination.value
            yield(Pair(denomination.key, unitsToTake))
        }
    }
}