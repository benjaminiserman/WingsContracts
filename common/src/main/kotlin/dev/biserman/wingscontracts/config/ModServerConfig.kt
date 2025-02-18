package dev.biserman.wingscontracts.config

import net.minecraftforge.common.ForgeConfigSpec

class ModServerConfig(builder: ForgeConfigSpec.Builder) {
    val denominations: ForgeConfigSpec.ConfigValue<String>

    init {
        builder.push("General")
        denominations =
            builder.comment("Comma-separated list of reward items that can be automatically converted by portals into other denominations. Multiple lists may be provided, separated by semicolons.")
                .define(
                    "denominations", defaultDenominations
                )
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
            numismatics:sun = 4096,
            test:xyz = 100;
        """.replace(Regex("\\s"), "").trimIndent()
    }
}