package dev.biserman.wingscontracts.neoforge

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.neoforge.compat.ForgeModCompat
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig as NeoForgeConfig

@Mod(WingsContractsMod.MOD_ID)
class WingsContractsModNeoForge(bus: IEventBus, container: ModContainer) {
    init {
        container.registerConfig(NeoForgeConfig.Type.SERVER, ModConfig.SERVER_SPEC)

        WingsContractsMod.init()
        ForgeModCompat.init(bus)
    }
}
