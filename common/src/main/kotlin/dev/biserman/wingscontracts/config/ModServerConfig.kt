package dev.biserman.wingscontracts.config

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.common.ForgeConfigSpec

class ModServerConfig(builder: ForgeConfigSpec.Builder) {
    val denominations: ForgeConfigSpec.ConfigValue<String>
    val contractGrowthFunction: ForgeConfigSpec.EnumValue<GrowthFunctionOptions>
    val availableContractsPoolRefreshPeriodMs: ForgeConfigSpec.LongValue
    val availableContractsPoolOptions: ForgeConfigSpec.IntValue
    val availableContractsPoolPicks: ForgeConfigSpec.IntValue
    val randomizeCraftedContracts: ForgeConfigSpec.BooleanValue
    val disableDefaultContractOptions: ForgeConfigSpec.BooleanValue
    val variance: ForgeConfigSpec.DoubleValue
    val rarityThresholdsString: ForgeConfigSpec.ConfigValue<String>

    // Contract Defaults
    val defaultRewardCurrencyId: ForgeConfigSpec.ConfigValue<String>
    val defaultRewardCurrency get() = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(defaultRewardCurrencyId.get()))
    val defaultRewardCurrencyUnit: ForgeConfigSpec.ConfigValue<String>
    val defaultRewardCurrencyMultiplier: ForgeConfigSpec.DoubleValue
    val defaultUnitsDemandedMultiplier: ForgeConfigSpec.DoubleValue
    val defaultCountPerUnitMultiplier: ForgeConfigSpec.DoubleValue
    val defaultCycleDurationMs: ForgeConfigSpec.LongValue
    val defaultAuthor: ForgeConfigSpec.ConfigValue<String>
    val defaultMaxLevel: ForgeConfigSpec.ConfigValue<Int>
    val defaultGrowthFactor: ForgeConfigSpec.DoubleValue

    init {
        builder.push("General")
        denominations =
            builder.comment("Comma-separated list of reward items that can be automatically converted by portals into other denominations. Multiple lists may be provided, separated by semicolons.")
                .define(
                    "denominations", defaultDenominations
                )

        contractGrowthFunction = builder.comment(
            """
            The function that determines how a contract's quantity demanded increases as it levels up. 
            LINEAR: unitsDemanded = baseUnitsDemanded + baseUnitsDemanded * (growthFactor - 1) * (level - 1)
            EXPONENTIAL: unitsDemanded = baseUnitsDemanded * growthFactor ** (level - 1)
            """.trimIndent()
        ).defineEnum("contractGrowthFunction", GrowthFunctionOptions.EXPONENTIAL)

        availableContractsPoolRefreshPeriodMs =
            builder.comment("The default time for the available Abyssal Contracts pool to refresh, in milliseconds. E.g.: 86400000 for one day, 604800000 for one week")
                .defineInRange("availableContractsPoolRefreshPeriodMs", 86400000L, 60_000, Long.MAX_VALUE)

        availableContractsPoolOptions =
            builder.comment("Determines how many Abyssal Contracts are available in the pool at any one time. Set to zero to disable the Abyssal Contracts pool.")
                .defineInRange("availableContractsPoolOptions", 0, 0, 10)

        availableContractsPoolPicks =
            builder.comment("Determines how many picks each player gets from the Abyssal Contracts pool per refresh period.")
                .defineInRange("availableContractsPoolPicks", 1, 0, Int.MAX_VALUE)

        randomizeCraftedContracts =
            builder.comment("If true, crafted contracts become a randomized usable Abyssal Contract. If false, they become an unusable Unknown Contract.")
                .define("randomizeCraftedContracts", true)

        disableDefaultContractOptions =
            builder.comment(
                """
                If true, skip all contract data files ending in "_default.json".
                Use this if you want to replace the default contract options with a custom data pack.
                """.trimIndent()
            ).define("disableDefaultContractOptions", false)

        variance =
            builder.comment(
                """
                The maximum distance a value of an Abyssal Contract from the pool can generate from its default values for countPerUnit, baseUnitsDemanded, and reward.
                Example: if variance is set to 0.2 and you have a contract in the pool configured to convert 10 iron ingots → 5 emeralds, then you might generate any of the following contracts:
                 - 8 iron ingots → 4 emeralds
                 - 8 iron ingots → 6 emeralds
                 - 12 iron ingots → 4 emeralds
                 - 12 iron ingots → 6 emeralds
                 - 11 iron ingots → 5 emeralds
                 - ... or anything else in-between
                """.trimIndent()
            )
                .defineInRange("variance", 0.33, 0.0, Double.MAX_VALUE)

        rarityThresholdsString =
            builder.comment("The max-level reward necessary to reach rarities Uncommon, Rare, and Epic respectively as a comma-separated list of integers.")
                .define("rarityThresholds", "16000,32000,64000")

        builder.pop()
        builder.push("Contract Defaults")

        defaultRewardCurrencyId =
            builder.comment("Loaded contracts with an unspecified or integer reward will have their reward ID set to this ID. Consider changing this to minecraft:diamond or numismatics:spur")
                .define("defaultRewardCurrencyId", "minecraft:emerald")

        defaultRewardCurrencyUnit =
            builder.comment(
                """
                If provided, serves as the format to display rewards denominated in the default reward currency.
                For example: \"$%d\" for dollars or \"%d¤\" for a more generic unit
                """.trimIndent()
            ).define("defaultRewardCurrencyUnit", "")

        defaultRewardCurrencyMultiplier =
            builder.comment("Datapacked contracts with an unspecified or integer reward will have their reward count multiplied by this factor, then rounded (minimum of 1).")
                .defineInRange("defaultRewardCurrencyMultiplier", 2.0, 0.0, Double.MAX_VALUE)

        defaultUnitsDemandedMultiplier =
            builder.comment("All new Abyssal Contracts pulled from the pool will have their base units demanded multiplied by this factor, then rounded (minimum of 1).")
                .defineInRange("defaultUnitsDemandedMultiplier", 0.25, 0.0, Double.MAX_VALUE)

        defaultCountPerUnitMultiplier =
            builder.comment("All new Abyssal Contracts pulled from the pool will have their count demanded per unit multiplied by this factor, then rounded (minimum of 1).")
                .defineInRange("defaultCountPerUnitMultiplier", 10.0, 0.0, Double.MAX_VALUE)

        defaultCycleDurationMs =
            builder.comment("The default length of a cycle period, in milliseconds. E.g.: 86400000 for one day, 604800000 for one week")
                .defineInRange("defaultCycleDurationMs", 86400000L, 60_000, Long.MAX_VALUE)

        defaultAuthor =
            builder.comment("The default author name for Abyssal Contracts")
                .define("defaultAuthor", "§kThe Abyss§r")

        defaultMaxLevel =
            builder.comment("The default max level for Abyssal Contracts. If negative or zero, the contract will have no max level.")
                .define("defaultMaxLevel", 5)

        defaultGrowthFactor = builder.comment(
            """
            The default growth factor for Abyssal Contracts.
            See contractGrowthFunction above to see how this is used.
            """.trimIndent()
        ).defineInRange("defaultGrowthFactor", 2.0, 0.00001, 100.0)

        builder.pop()
    }


    companion object {
        val defaultDenominations = """
            minecraft:emerald = 1, 
            minecraft:emerald_block = 9;
            
            minecraft:lapis_lazuli = 1,
            minecraft:lapis_block = 9;
            
            minecraft:gold_nugget = 1,
            minecraft:gold_ingot = 9,
            minecraft:gold_block = 81
            
            minecraft:diamond = 1, 
            minecraft:diamond_block = 9;
            
            numismatics:spur = 1,
            numismatics:bevel = 8,
            numismatics:sprocket = 16,
            numismatics:cog = 64,
            numismatics:crown = 512,
            numismatics:sun = 4096;
        """.replace(Regex("\\s"), "").trimIndent()
    }
}