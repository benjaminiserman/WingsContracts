package dev.biserman.wingscontracts.config

import net.neoforged.neoforge.common.ModConfigSpec

object ModConfig {
    val SERVER_SPEC: ModConfigSpec
    val SERVER: ModServerConfig
    val COMMON_SPEC: ModConfigSpec
    val COMMON: ModCommonConfig

    init {
        val (serverConfig, serverConfigSpec) = ModConfigSpec.Builder().configure(::ModServerConfig)
        SERVER_SPEC = serverConfigSpec
        SERVER = serverConfig

        val (commonConfig, commonConfigSpec) = ModConfigSpec.Builder().configure(::ModCommonConfig)
        COMMON_SPEC = commonConfigSpec
        COMMON = commonConfig
    }
}