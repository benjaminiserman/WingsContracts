package dev.biserman.wingscontracts.fabric

import dev.biserman.wingscontracts.WingsContractsMod
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry
import net.fabricmc.api.ModInitializer
import net.minecraftforge.fml.config.ModConfig
import dev.biserman.wingscontracts.config.ModConfig as ContractConfig

object WingsContractsModFabric : ModInitializer {
    override fun onInitialize() {
        WingsContractsMod.init()

        ForgeConfigRegistry.INSTANCE.register(WingsContractsMod.MOD_ID, ModConfig.Type.SERVER, ContractConfig.SERVER_SPEC)
        ForgeConfigRegistry.INSTANCE.register(WingsContractsMod.MOD_ID, ModConfig.Type.COMMON, ContractConfig.COMMON_SPEC)
    }
}
