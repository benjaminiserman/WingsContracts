package dev.biserman.wingscontracts.config

import net.minecraftforge.common.ForgeConfigSpec

object ModConfig {
    var SERVER_SPEC: ForgeConfigSpec
    var SERVER: ModServerConfig

    init {
        val (serverConfig, serverConfigSpec) = ForgeConfigSpec.Builder().configure(::ModServerConfig)
        SERVER_SPEC = serverConfigSpec
        SERVER = serverConfig
    }
}