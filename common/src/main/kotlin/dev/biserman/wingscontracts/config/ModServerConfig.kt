package dev.biserman.wingscontracts.config

import net.minecraftforge.common.ForgeConfigSpec

class ModServerConfig(builder: ForgeConfigSpec.Builder) {
    val denominations: ForgeConfigSpec.ConfigValue<String>
    val availableContractsPoolRefreshPeriodMs: ForgeConfigSpec.LongValue
    val availableContractsPoolOptions: ForgeConfigSpec.IntValue

    // Contract Defaults
    val defaultRewardCurrencyId: ForgeConfigSpec.ConfigValue<String>
    val defaultRewardCurrencyMultiplier: ForgeConfigSpec.DoubleValue
    val defaultUnitsDemandedMultiplier: ForgeConfigSpec.DoubleValue
    val defaultCycleDurationMs: ForgeConfigSpec.LongValue
    val defaultAuthor: ForgeConfigSpec.ConfigValue<String>
    val defaultMaxLevel: ForgeConfigSpec.IntValue
    val defaultGrowthFactor: ForgeConfigSpec.DoubleValue

    init {
        builder.push("General")
        denominations =
            builder.comment("Comma-separated list of reward items that can be automatically converted by portals into other denominations. Multiple lists may be provided, separated by semicolons.")
                .define(
                    "denominations", defaultDenominations
                )

        availableContractsPoolRefreshPeriodMs =
            builder.comment("The default time for the available Abyssal Contracts pool to refresh, in milliseconds. E.g.: 86400000 for one day, 604800000 for one week")
                .defineInRange("availableContractsPoolRefreshPeriodMs", 60_000, Long.MAX_VALUE, 604800000L)

        availableContractsPoolOptions =
            builder.comment("Determines how many Abyssal Contracts are available in the pool at any one time.")
                .defineInRange("availableContractsPoolOptions", 0, 10, 5)

        builder.pop()
        builder.push("Contract Defaults")

        defaultRewardCurrencyId =
            builder.comment("Datapacked contracts with a reward ID of minecraft:air will have their reward ID converted to this ID. Consider changing this to minecraft:diamond or numismatics:spur")
                .define("defaultRewardCurrencyId", "minecraft:emerald")

        defaultRewardCurrencyMultiplier =
            builder.comment("Datapacked contracts with a reward ID of minecraft:air will have their reward count multiplied by this factor, then rounded up.")
                .defineInRange("defaultRewardCurrencyMultiplier", 0.0, Double.MAX_VALUE, 1.0)

        defaultUnitsDemandedMultiplier =
            builder.comment("All new Abyssal Contracts pulled from the pool will have their base units demanded multiplied by this factor, then rounded up.")
                .defineInRange("defaultUnitsDemandedMultiplier", 0.0, Double.MAX_VALUE, 1.0)

        defaultCycleDurationMs =
            builder.comment("The default length of a cycle period, in milliseconds. E.g.: 86400000 for one day, 604800000 for one week")
                .defineInRange("defaultCycleDurationMs", 60_000, Long.MAX_VALUE, 604800000L)

        defaultAuthor =
            builder.comment("The default author name for Abyssal Contracts")
                .define("defaultAuthor", "ẗ̸̠̰́͑h̴̢͓͛̈́e̷͇̓͌ ̷̗̊̀a̵̳̓b̷͓̖̅̑y̷̛̥̮s̷͔̚s̸͖͚͆")

        defaultMaxLevel = builder.comment("The default max level for Abyssal Contracts")
            .defineInRange("defaultMaxLevel", 1, Int.MAX_VALUE, 10)

        defaultGrowthFactor = builder.comment(
            """
            The default growth factor for Abyssal Contracts.
            The number of units demanded for an Abyssal Contract = baseUnitsDemanded + floor(baseUnitsDemanded * (level - 1) * quantityGrowthFactor)
            """.trimMargin()
        ).defineInRange("defaultGrowthFactor", 0.00001, 100.0, 1.0)

        builder.pop()
    }

    companion object {
        val defaultDenominations = """
            minecraft:emerald = 1, 
            minecraft:emerald_block = 9;
            
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