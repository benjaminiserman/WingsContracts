package dev.biserman.wingscontracts.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import dev.architectury.platform.Platform
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.core.AbyssalContract
import dev.biserman.wingscontracts.core.Contract.Companion.name
import dev.biserman.wingscontracts.core.Contract.Companion.requiresAll
import dev.biserman.wingscontracts.core.Contract.Companion.requiresAny
import dev.biserman.wingscontracts.core.Contract.Companion.requiresNot
import dev.biserman.wingscontracts.core.Contract.Companion.targetItemKeys
import dev.biserman.wingscontracts.core.Contract.Companion.targetTagKeys
import dev.biserman.wingscontracts.tag.ContractTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller

val GSON: Gson = (GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create()

object AvailableContractsManager : SimpleJsonResourceReloadListener(GSON, "contracts") {
    private var allAvailableContracts = listOf<ContractTag>()
    private var nonDefaultAvailableContracts = listOf<ContractTag>()
    private val availableContracts
        get() = if (ModConfig.SERVER.disableDefaultContractOptions.get()) {
            nonDefaultAvailableContracts
        } else {
            allAvailableContracts
        }

    fun randomTag() =
        if (availableContracts.isEmpty()) {
            WingsContractsMod.LOGGER.warn("Available contracts pool is empty, returning unknown contract.")
            val contract = ContractTag(CompoundTag())
            contract.name = Component.translatable("${WingsContractsMod.MOD_ID}.contract.unknown").string
            contract
        } else {
            ContractTag(availableContracts.random().tag.copy())
        }


    override fun apply(
        jsonMap: Map<ResourceLocation, JsonElement>,
        resourceManager: ResourceManager,
        profilerFiller: ProfilerFiller
    ) {
        WingsContractsMod.LOGGER.info("Building available contracts pool...")
        val buildAvailableContracts = mutableListOf<ContractTag>()
        val buildNonDefaultAvailableContracts = mutableListOf<ContractTag>()

        var skippedBecauseUnloaded = 0
        for ((resourceLocation, json) in jsonMap) {
            if (resourceLocation.path.startsWith("_")) {
                continue
            }

            val isDefault = resourceLocation.path.endsWith("_default")

            try {
                val parsedContracts =
                    if (json.isJsonObject) {
                        listOf(AbyssalContract.fromJson(json.asJsonObject))
                    } else json.asJsonArray.map {
                        AbyssalContract.fromJson(it.asJsonObject)
                    }

                for (contract in parsedContracts) {
                    // skip contracts that only apply to unloaded mods
                    val allItemsFailedLoad = contract.targetItemKeys != null && (contract.targetItemKeys ?: listOf())
                        .all { it.contains(':') && !Platform.isModLoaded(it.split(":")[0]) }
                    val allTagsFailedLoad = contract.targetTagKeys != null && (contract.targetTagKeys ?: listOf())
                        .all { it.contains(':') && !Platform.isModLoaded(it.split(":")[0].trimStart('#')) }
                    val allFailedLoad = allItemsFailedLoad && allTagsFailedLoad
                    val allRequiredModsLoaded = contract.requiresAll.isNullOrBlank()
                            || contract.requiresAll!!.split(',').all { Platform.isModLoaded(it) }
                    val anyRequiredModsLoaded = contract.requiresAny.isNullOrBlank()
                            || contract.requiresAny!!.split(',').any { Platform.isModLoaded(it) }

                    if (allFailedLoad || !allRequiredModsLoaded || !anyRequiredModsLoaded) {
                        if (!isDefault) {
                            WingsContractsMod.LOGGER.warn("Skipped custom contract $contract because required mod was not found.")
                        }
                        skippedBecauseUnloaded++
                        continue
                    }

                    val blockedModFound = !contract.requiresNot.isNullOrBlank()
                            && contract.requiresNot!!.split(',').any { Platform.isModLoaded(it) }
                    if (blockedModFound) {
                        if (!isDefault) {
                            WingsContractsMod.LOGGER.warn("Skipped custom contract $contract because blocked mod was found.")
                        }
                        continue
                    }

                    if (AbyssalContract.load(contract).isValid) {
                        buildAvailableContracts.add(contract)
                        if (!isDefault) {
                            buildNonDefaultAvailableContracts.add(contract)
                        }
                    } else {
                        WingsContractsMod.LOGGER.warn("Found invalid contract $contract in $resourceLocation")
                    }
                }


            } catch (e: Exception) {
                WingsContractsMod.LOGGER.error("Error while loading available contracts at $resourceLocation", e)
            }
        }

        if (skippedBecauseUnloaded != 0) {
            WingsContractsMod.LOGGER.info("Skipped $skippedBecauseUnloaded contracts from unloaded mods.")
        }

        allAvailableContracts = buildAvailableContracts.toList()
        nonDefaultAvailableContracts = buildNonDefaultAvailableContracts.toList()
    }
}