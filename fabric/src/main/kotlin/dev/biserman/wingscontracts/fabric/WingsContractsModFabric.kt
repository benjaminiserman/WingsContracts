package dev.biserman.wingscontracts.fabric

import dev.biserman.wingscontracts.WingsContractsMod
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry
import net.fabricmc.api.ModInitializer
import net.neoforged.fml.config.ModConfig
import dev.biserman.wingscontracts.config.ModConfig as ContractConfig

object WingsContractsModFabric : ModInitializer {
    override fun onInitialize() {
        WingsContractsMod.init()

        NeoForgeConfigRegistry.INSTANCE.register(WingsContractsMod.MOD_ID, ModConfig.Type.SERVER, ContractConfig.SERVER_SPEC)
        NeoForgeConfigRegistry.INSTANCE.register(WingsContractsMod.MOD_ID, ModConfig.Type.COMMON, ContractConfig.COMMON_SPEC)
    }
}
