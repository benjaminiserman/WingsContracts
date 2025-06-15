package dev.biserman.wingscontracts.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import dev.architectury.platform.Platform
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.core.AbyssalContract
import dev.biserman.wingscontracts.core.Contract.Companion.countPerUnit
import dev.biserman.wingscontracts.core.Contract.Companion.name
import dev.biserman.wingscontracts.core.Contract.Companion.requiresAll
import dev.biserman.wingscontracts.core.Contract.Companion.requiresAny
import dev.biserman.wingscontracts.core.Contract.Companion.requiresNot
import dev.biserman.wingscontracts.core.Contract.Companion.targetBlockTagKeys
import dev.biserman.wingscontracts.core.Contract.Companion.targetBlockTags
import dev.biserman.wingscontracts.core.Contract.Companion.targetConditionsKeys
import dev.biserman.wingscontracts.core.Contract.Companion.targetItemKeys
import dev.biserman.wingscontracts.core.Contract.Companion.targetItems
import dev.biserman.wingscontracts.core.Contract.Companion.targetTagKeys
import dev.biserman.wingscontracts.core.Contract.Companion.targetTags
import dev.biserman.wingscontracts.nbt.ContractTag
import dev.biserman.wingscontracts.nbt.ContractTagHelper
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull
import kotlin.math.pow

val GSON: Gson = (GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create()

class RewardBagEntry(val item: ItemStack, val value: Double, val weight: Int, val formatString: String? = null)

object ContractDataReloadListener : SimpleJsonResourceReloadListener(GSON, "wingscontracts") {
    private val allAvailableContracts = mutableListOf<ContractTag>()
    private val nonDefaultAvailableContracts = mutableListOf<ContractTag>()
    val availableContracts
        get() = if (ModConfig.SERVER.disableDefaultContractOptions.get()) {
            nonDefaultAvailableContracts.toList()
        } else {
            allAvailableContracts.toList()
        }

    private val allDefaultRewards = mutableListOf<RewardBagEntry>()
    private val nonDefaultDefaultRewards =
        mutableListOf<RewardBagEntry>() // funny name, but it refers to custom-specified default rewards
    val defaultRewards
        get() = if (ModConfig.SERVER.disableDefaultContractOptions.get()) {
            nonDefaultDefaultRewards.toList()
        } else {
            allDefaultRewards.toList()
        }

    fun valueReward(itemStack: ItemStack): Double {
        val reward = defaultRewards.find { it.item.item == itemStack.item } ?: return 0.0
        return itemStack.count * reward.value / reward.item.count
    }

    private val fullRewardBlocklist = mutableListOf<String>()
    private val nonDefaultRewardBlocklist = mutableListOf<String>()
    val rewardBlocklist
        get() = if (ModConfig.SERVER.disableDefaultContractOptions.get()) {
            nonDefaultRewardBlocklist.toList()
        } else {
            fullRewardBlocklist.toList()
        }

    var areContractsValidated = false

    override fun prepare(
        resourceManager: ResourceManager,
        profilerFiller: ProfilerFiller
    ): Map<ResourceLocation, JsonElement> {
        allAvailableContracts.clear()
        nonDefaultAvailableContracts.clear()
        allDefaultRewards.clear()
        nonDefaultDefaultRewards.clear()
        fullRewardBlocklist.clear()
        nonDefaultRewardBlocklist.clear()

        return super.prepare(resourceManager, profilerFiller)
    }

    fun randomTag(): ContractTag {
        tryValidateContracts()

        if (availableContracts.isEmpty()) {
            WingsContractsMod.LOGGER.warn("Available contracts pool is empty, returning unknown contract.")
            val contract = ContractTag(CompoundTag())
            contract.name = Component.translatable("${WingsContractsMod.MOD_ID}.contract.unknown").string
            return contract
        } else {
            return ContractTag(availableContracts.random().tag.copy())
        }
    }

    override fun apply(
        jsonMap: Map<ResourceLocation, JsonElement>,
        resourceManager: ResourceManager,
        profilerFiller: ProfilerFiller
    ) {
        areContractsValidated = false
        WingsContractsMod.LOGGER.info("Building abyssal contracts pool...")
        var skippedBecauseUnloaded = 0
        for ((resourceLocation, json) in jsonMap) {
            if (resourceLocation.path.startsWith("_")) {
                continue
            }

            WingsContractsMod.LOGGER.info("...$resourceLocation")

            val isDefault = resourceLocation.path.endsWith("_default")

            try {
                val jsonObject = json.asJsonObject
                val parsedContracts = jsonObject.get("contracts")?.asJsonArray?.map {
                    ContractTag.fromJson(it.asJsonObject)
                } ?: listOf()
                skippedBecauseUnloaded += validateContracts(parsedContracts, resourceLocation, isDefault)

                val parsedDefaultRewards = jsonObject.get("rewards")?.asJsonArray?.mapNotNull {
                    val itemStack = ItemStack.parse(
                        ContractTagHelper.registryAccess!!,
                        JsonOps.INSTANCE.convertTo(
                            NbtOps.INSTANCE,
                            it.asJsonObject.get("item")
                        ) as CompoundTag
                    ).getOrNull()

                    if (itemStack == null) {
                        WingsContractsMod.LOGGER.warn("Could not find itemStack ${it.asJsonObject.get("item")}")
                        return@mapNotNull null
                    }

                    RewardBagEntry(
                        itemStack,
                        it.asJsonObject.get("value").asDouble,
                        it.asJsonObject.get("weight").asInt,
                        if (it.asJsonObject.has("formatString")) {
                            it.asJsonObject.get("formatString").asString
                        } else {
                            null
                        }
                    )
                } ?: listOf()

                allDefaultRewards.addAll(parsedDefaultRewards)
                if (!isDefault) {
                    nonDefaultDefaultRewards.addAll(parsedDefaultRewards)
                }

                val parsedRewardBlocklist = jsonObject.get("blockedReplacementRewards")
                    ?.asJsonArray?.map { it.asString } ?: listOf()
                fullRewardBlocklist.addAll(parsedRewardBlocklist)
                if (!isDefault) {
                    nonDefaultRewardBlocklist.addAll(parsedRewardBlocklist)
                }
            } catch (e: Exception) {
                WingsContractsMod.LOGGER.error("Error while loading available contracts at $resourceLocation", e)
            }
        }

        if (skippedBecauseUnloaded != 0) {
            WingsContractsMod.LOGGER.info("Skipped $skippedBecauseUnloaded contracts from unloaded mods.")
        }
    }

    fun validateContracts(
        parsedContracts: List<ContractTag>,
        resourceLocation: ResourceLocation,
        isDefault: Boolean
    ): Int {
        var skippedBecauseUnloaded = 0
        for (contract in parsedContracts) {
            // skip contracts that only apply to unloaded mods
            val targetItemKeys = contract.targetItemKeys ?: listOf()
            val targetTagKeys = contract.targetTagKeys ?: listOf()
            val targetBlockTagKeys = contract.targetBlockTagKeys ?: listOf()

            val allItemsFailedLoad = targetItemKeys
                .all { it.contains(':') && !Platform.isModLoaded(it.split(":")[0]) }
            val allBlockTagsFailedLoad = targetBlockTagKeys
                .all { it.contains(':') && !Platform.isModLoaded(it.split(":")[0].trimStart('#')) }

            val isJustCondition = !contract.targetConditionsKeys.isNullOrBlank()
                    && targetItemKeys.isEmpty()
                    && targetTagKeys.isEmpty()
                    && targetBlockTagKeys.isEmpty()

            val allFailedLoad = allItemsFailedLoad
                    && allBlockTagsFailedLoad
                    && !isJustCondition
                    && targetTagKeys.isEmpty()

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
                allAvailableContracts.add(contract)
                if (!isDefault) {
                    nonDefaultAvailableContracts.add(contract)
                }
            } else {
                WingsContractsMod.LOGGER.warn("Found invalid contract $contract in $resourceLocation")
            }
        }

        return skippedBecauseUnloaded
    }

    fun tryValidateContracts() {
        if (!areContractsValidated) {
            WingsContractsMod.LOGGER.info("Checking for invalid contracts...")
            listOf(allAvailableContracts, nonDefaultAvailableContracts).forEach { contractList ->
                removeEmptyTags(contractList)
                removeImpossibleUnitDemands(contractList)
            }
            areContractsValidated = true
        }
    }

    fun removeEmptyTags(contractList: MutableList<ContractTag>) {
        contractList.removeIf { contract ->
            val itemTags = contract.targetTags ?: listOf()
            val blockTags = contract.targetBlockTags ?: listOf()
            val targetItems = contract.targetItems ?: listOf()

            if (itemTags.isEmpty() && blockTags.isEmpty()) {
                return@removeIf false
            }

            if (itemTags.all { BuiltInRegistries.ITEM.getTagOrEmpty(it).count() == 0 }
                && blockTags.all { BuiltInRegistries.BLOCK.getTagOrEmpty(it).count() == 0 }
                && targetItems.isEmpty()) {
                WingsContractsMod.LOGGER.warn("Removing empty tag contract: $contract")
                return@removeIf true
            }

            return@removeIf false
        }
    }

    // remove contracts that would be impossible to fulfill due to the interaction between their target's max stack size and
    // the container size of the Contract Portal
    fun removeImpossibleUnitDemands(contractList: MutableList<ContractTag>) {
        contractList.removeIf { contract ->
            val countPerUnit = contract.countPerUnit ?: 64

            val itemTags = contract.targetTags ?: listOf()
            val blockTags = contract.targetBlockTags ?: listOf()
            val targetItems = contract.targetItems ?: listOf()

            val maxStackSize =
                itemTags.flatMap { BuiltInRegistries.ITEM.getTagOrEmpty(it).map { it.value().defaultMaxStackSize } }
                    .plus(blockTags.flatMap {
                        BuiltInRegistries.BLOCK.getTagOrEmpty(it).map { it.value().asItem().defaultMaxStackSize }
                    })
                    .plus(targetItems.map { it.defaultMaxStackSize })
                    .maxOrNull() ?: 64

            val defaultCountPerUnitMultiplier = ModConfig.SERVER.defaultCountPerUnitMultiplier.get()
            val variance = ModConfig.SERVER.variance.get()
            val portalContainerSize = ModConfig.SERVER.contractPortalInputSlots.get()

            if (countPerUnit * defaultCountPerUnitMultiplier * (1 + variance).pow(2) > portalContainerSize * maxStackSize) {
                WingsContractsMod.LOGGER.warn("Removing contract with countPerUnit too large for contract portal: $contract")
                return@removeIf true
            }

            return@removeIf false
        }
    }
}