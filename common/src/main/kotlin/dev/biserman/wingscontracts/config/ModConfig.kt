package dev.biserman.wingscontracts.config

import net.minecraftforge.common.ForgeConfigSpec

object ModConfig {
    val SERVER_SPEC: ForgeConfigSpec
    val SERVER: ModServerConfig
    val COMMON_SPEC: ForgeConfigSpec
    val COMMON: ModCommonConfig

    init {
        val (serverConfig, serverConfigSpec) = ForgeConfigSpec.Builder().configure(::ModServerConfig)
        SERVER_SPEC = serverConfigSpec
        SERVER = serverConfig

        val (commonConfig, commonConfigSpec) = ForgeConfigSpec.Builder().configure(::ModCommonConfig)
        COMMON_SPEC = commonConfigSpec
        COMMON = commonConfig
    }
}