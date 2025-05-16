package dev.biserman.wingscontracts.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import dev.architectury.platform.Platform
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.core.AbyssalContract
import dev.biserman.wingscontracts.core.Contract
import dev.biserman.wingscontracts.core.Contract.Companion.name
import dev.biserman.wingscontracts.core.Contract.Companion.requiresAll
import dev.biserman.wingscontracts.core.Contract.Companion.requiresAny
import dev.biserman.wingscontracts.core.Contract.Companion.requiresNot
import dev.biserman.wingscontracts.core.Contract.Companion.targetBlockTagKeys
import dev.biserman.wingscontracts.core.Contract.Companion.targetConditionsKeys
import dev.biserman.wingscontracts.core.Contract.Companion.targetItemKeys
import dev.biserman.wingscontracts.core.Contract.Companion.targetTagKeys
import dev.biserman.wingscontracts.nbt.ContractTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.item.ItemStack

val GSON: Gson = (GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create()

class RewardBagEntry(val item: ItemStack, val value: Double, val weight: Int, val formatString: String? = null)

object AvailableContractsManager : SimpleJsonResourceReloadListener(GSON, "wingscontracts") {
    private var allAvailableContracts = listOf<ContractTag>()
    private var nonDefaultAvailableContracts = listOf<ContractTag>()
    val availableContracts
        get() = if (ModConfig.SERVER.disableDefaultContractOptions.get()) {
            nonDefaultAvailableContracts
        } else {
            allAvailableContracts
        }

    private var allDefaultRewards = listOf<RewardBagEntry>()
    private var nonDefaultDefaultRewards =
        listOf<RewardBagEntry>() // funny name, but it refers to custom-specified default rewards
    val defaultRewards
        get() = if (ModConfig.SERVER.disableDefaultContractOptions.get()) {
            nonDefaultDefaultRewards
        } else {
            allDefaultRewards
        }
    val defaultRewardBagWeightSum by lazy { defaultRewards.sumOf { it.weight } }

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
        val buildDefaultRewards = mutableListOf<RewardBagEntry>()
        val buildNonDefaultDefaultRewards = mutableListOf<RewardBagEntry>()

        var skippedBecauseUnloaded = 0
        for ((resourceLocation, json) in jsonMap) {
            if (resourceLocation.path.startsWith("_")) {
                continue
            }

            WingsContractsMod.LOGGER.info(resourceLocation)

            val isDefault = resourceLocation.path.endsWith("_default")

            try {
                val jsonObject = json.asJsonObject
                val parsedContracts = jsonObject.get("contracts")?.asJsonArray?.map {
                    Contract.fromJson(it.asJsonObject)
                } ?: listOf()

                val parsedDefaultRewards = jsonObject.get("rewards")?.asJsonArray?.map {
                    RewardBagEntry(
                        ItemStack.of(
                            JsonOps.INSTANCE.convertTo(
                                NbtOps.INSTANCE,
                                it.asJsonObject.get("item")
                            ) as CompoundTag
                        ),
                        it.asJsonObject.get("value").asDouble,
                        it.asJsonObject.get("weight").asInt,
                        if (it.asJsonObject.has("formatString")) {
                            it.asJsonObject.get("formatString").asString
                        } else {
                            null
                        }
                    )
                } ?: listOf()

                buildDefaultRewards.addAll(parsedDefaultRewards)
                if (!isDefault) {
                    buildNonDefaultDefaultRewards.addAll(parsedDefaultRewards)
                }

                for (contract in parsedContracts) {
                    // skip contracts that only apply to unloaded mods
                    val allItemsFailedLoad = (contract.targetItemKeys ?: listOf())
                        .all { it.contains(':') && !Platform.isModLoaded(it.split(":")[0]) }
                    val allTagsFailedLoad = (contract.targetTagKeys ?: listOf())
                        .all { it.contains(':') && !Platform.isModLoaded(it.split(":")[0].trimStart('#')) }
                    val allBlockTagsFailedLoad = (contract.targetBlockTagKeys ?: listOf())
                        .all { it.contains(':') && !Platform.isModLoaded(it.split(":")[0].trimStart('#')) }
                    val allFailedLoad = allItemsFailedLoad
                            && allTagsFailedLoad
                            && allBlockTagsFailedLoad
                            && contract.targetConditionsKeys.isNullOrBlank()

                    // check for required mods
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
        allDefaultRewards = buildDefaultRewards.toList()
        nonDefaultDefaultRewards = buildNonDefaultDefaultRewards.toList()
    }
}