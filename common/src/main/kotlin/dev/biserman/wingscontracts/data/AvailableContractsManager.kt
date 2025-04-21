package dev.biserman.wingscontracts.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import dev.architectury.platform.Platform
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.api.AbyssalContract
import dev.biserman.wingscontracts.api.Contract.Companion.name
import dev.biserman.wingscontracts.api.Contract.Companion.targetItemKeys
import dev.biserman.wingscontracts.api.Contract.Companion.targetTagKeys
import dev.biserman.wingscontracts.config.ModConfig
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
                    val allItemsFailedLoad = (contract.targetItemKeys ?: listOf())
                        .all { it.contains(':') && !Platform.isModLoaded(it.split(":")[0]) }
                    val allTagsFailedLoad = (contract.targetTagKeys ?: listOf())
                        .all { it.contains(':') && !Platform.isModLoaded(it.split(":")[0].trimStart('#')) }
                    if (allItemsFailedLoad && allTagsFailedLoad
                    ) {
                        WingsContractsMod.LOGGER.warn("Skipping contract $contract of unloaded mod")
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

        allAvailableContracts = buildAvailableContracts.toList()
        nonDefaultAvailableContracts = buildNonDefaultAvailableContracts.toList()
    }
}