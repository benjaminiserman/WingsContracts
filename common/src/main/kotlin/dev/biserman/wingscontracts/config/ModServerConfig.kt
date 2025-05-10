package dev.biserman.wingscontracts.config

import net.minecraftforge.common.ForgeConfigSpec

class ModServerConfig(builder: ForgeConfigSpec.Builder) {
    val denominations: ForgeConfigSpec.ConfigValue<String>
    val contractGrowthFunction: ForgeConfigSpec.EnumValue<GrowthFunctionOptions>
    val availableContractsPoolRefreshPeriodMs: ForgeConfigSpec.LongValue
    val availableContractsPoolOptions: ForgeConfigSpec.IntValue
    val availableContractsPoolPicks: ForgeConfigSpec.IntValue
    val allowBlankContractInitialization: ForgeConfigSpec.BooleanValue
    val disableDefaultContractOptions: ForgeConfigSpec.BooleanValue
    val variance: ForgeConfigSpec.DoubleValue
    val replaceRewardWithRandomPercent: ForgeConfigSpec.DoubleValue
    val replaceRewardWithRandomFactor: ForgeConfigSpec.DoubleValue
    val replaceRewardWithRandomBlocklist: ForgeConfigSpec.ConfigValue<String>
    val rarityThresholdsString: ForgeConfigSpec.ConfigValue<String>

    // Contract Defaults
    val defaultRewards: ForgeConfigSpec.ConfigValue<String>
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
                .defineInRange("availableContractsPoolOptions", 10, 0, 10)

        availableContractsPoolPicks =
            builder.comment("Determines how many picks each player gets from the Abyssal Contracts pool per refresh period.")
                .defineInRange("availableContractsPoolPicks", 1, 0, Int.MAX_VALUE)

        allowBlankContractInitialization =
            builder.comment("If true, Blank Abyssal Contracts can be right-clicked to become a randomized usable Abyssal Contract.")
                .define("allowBlankContractInitialization", true)

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

        replaceRewardWithRandomPercent =
            builder.comment("This percentage of Abyssal Contracts generated that are set to the default reward currency will instead have their reward switched to a random input from another contract.")
                .defineInRange("replaceRewardWithRandomPercent", 0.8, 0.0, 1.0)

        replaceRewardWithRandomFactor =
            builder.comment("The reward from an Abyssal Contract with its reward randomly replaced will have its count multiplied by this factor.")
                .defineInRange("replaceRewardWithRandomFactor", 0.5, 0.0, Double.MAX_VALUE)

        replaceRewardWithRandomBlocklist =
            builder.comment("Items on this comma-separated list cannot appear as randomly replaced rewards in contracts.")
                .define(
                    "replaceRewardWithRandomBlocklist",
                    listOf(
                        "minecraft:potion",
                        "minecraft:splash_potion",
                        "minecraft:lingering_potion",
                        "minecraft:enchanted_book",
                        "botania:brew_vial",
                        "botania:brew_flask"
                    ).joinToString(",")
                )

        rarityThresholdsString =
            builder.comment("The max-level reward necessary to reach rarities Uncommon, Rare, and Epic respectively as a comma-separated list of integers.")
                .define("rarityThresholds", "16000,32000,64000")

        builder.pop()
        builder.push("Contract Defaults")

        defaultRewards =
            builder.comment(
                """
                Loaded contracts with an unspecified or integer reward will use one of these rewards instead. 
                Semicolon-separated list in the format "namespace:id,value_double,weight_int,format_string(optional);"
                Consider changing this to "wingscontracts:abyssal_coin,1,1,$%d" or "numismatics:spur,1,1,%d¤"
                """
            )
                .define(
                    "defaultRewards", listOf(
                        "minecraft:gold_nugget,0.25,2",
                        "minecraft:emerald,0.5,4",
                        "minecraft:lapis_lazuli,4,1",
                    ).joinToString(";")
                )

        defaultRewardCurrencyMultiplier =
            builder.comment("Datapacked contracts with an unspecified or integer reward will have their reward count multiplied by this factor, then rounded (minimum of 1).")
                .defineInRange("defaultRewardCurrencyMultiplier", 1.0, 0.0, Double.MAX_VALUE)

        defaultUnitsDemandedMultiplier =
            builder.comment("All new Abyssal Contracts pulled from the pool will have their base units demanded multiplied by this factor, then rounded (minimum of 1).")
                .defineInRange("defaultUnitsDemandedMultiplier", 0.25, 0.0, Double.MAX_VALUE)

        defaultCountPerUnitMultiplier =
            builder.comment("All new Abyssal Contracts pulled from the pool will have their count demanded per unit multiplied by this factor, then rounded (minimum of 1).")
                .defineInRange("defaultCountPerUnitMultiplier", 1.0, 0.0, Double.MAX_VALUE)

        defaultCycleDurationMs =
            builder.comment("The default length of a cycle period, in milliseconds. E.g.: 86400000 for one day, 604800000 for one week")
                .defineInRange("defaultCycleDurationMs", 86400000L, 60_000, Long.MAX_VALUE)

        defaultAuthor =
            builder.comment("The default author name for Abyssal Contracts")
                .define("defaultAuthor", "§kThe Abyss§r")

        defaultMaxLevel =
            builder.comment("The default max level for Abyssal Contracts. If negative or zero, the contract will have no max level.")
                .define("defaultMaxLevel", 10)

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