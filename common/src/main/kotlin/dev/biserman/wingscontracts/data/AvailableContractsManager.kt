package dev.biserman.wingscontracts.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.api.AbyssalContract
import dev.biserman.wingscontracts.tag.ContractTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller

val GSON: Gson = (GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create()

object AvailableContractsManager : SimpleJsonResourceReloadListener(GSON, "contracts") {
    private var availableContracts = listOf<ContractTag>()

    fun random() = if (availableContracts.isEmpty()) {
        ContractTag(CompoundTag())
    } else {
        availableContracts.random()
    }

    override fun apply(
        jsonMap: Map<ResourceLocation, JsonElement>,
        resourceManager: ResourceManager,
        profilerFiller: ProfilerFiller
    ) {
        WingsContractsMod.LOGGER.info("applying available contracts $$$ KOMENCI ${jsonMap.size}")
        val buildAvailableContracts = mutableListOf<ContractTag>()

        for ((resourceLocation, json) in jsonMap) {
            if (resourceLocation.path.startsWith("_")) {
                continue
            }

            WingsContractsMod.LOGGER.info("found $resourceLocation")

            try {
                if (json.isJsonObject) {
                    buildAvailableContracts.add(AbyssalContract.fromJson(json.asJsonObject))
                } else {
                    buildAvailableContracts.addAll(json.asJsonArray.map { AbyssalContract.fromJson(it.asJsonObject) })
                }
            } catch (e: Exception) {
                WingsContractsMod.LOGGER.error("Error while loading available contracts at $resourceLocation", e)
            }
        }

        availableContracts = buildAvailableContracts.toList()
    }
}