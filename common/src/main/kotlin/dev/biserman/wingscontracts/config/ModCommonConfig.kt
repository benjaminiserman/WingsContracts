package dev.biserman.wingscontracts.config

import net.minecraftforge.common.ForgeConfigSpec

class ModCommonConfig(builder: ForgeConfigSpec.Builder) {
    init {
        builder.push("""
            This mod is entirely configured through server configs and datapacks.
            
            You can find the server config in saves/YOUR_WORLD/serverconfig/wingscontracts-server.toml
            Alternatively, you can define the default server config for all worlds in defaultconfigs/wingscontracts-server.toml
            You may need to copy the config from an existing world to do so.
            
            The available contracts pool is determined via datapacks defined under YOUR_WORLD/datapacks/wingscontracts/contracts/YOUR_CONTRACT_FILE.json
            The default available contracts datapack can be found here: https://github.com/benjaminiserman/WingsContracts/blob/main/common/src/main/resources/data/wingscontracts/contracts/contracts.json
            Rewards with the id minecraft:air will be converted to defaultRewardCurrency from the server config.
        """.trimMargin())
    }
}