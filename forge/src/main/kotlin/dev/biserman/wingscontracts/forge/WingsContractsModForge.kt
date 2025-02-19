package dev.biserman.wingscontracts.forge

import dev.architectury.platform.forge.EventBuses
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.compat.CompatMods
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import dev.biserman.wingscontracts.config.ModConfig as WingsContractsConfig

@Mod(WingsContractsMod.MOD_ID)
class WingsContractsModForge {
    init {
        @Suppress("removal")
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, WingsContractsConfig.SERVER_SPEC)

        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(WingsContractsMod.MOD_ID, MOD_BUS)

        // Run our common setup.
        WingsContractsMod.init()

        if (ModList.get().isLoaded(CompatMods.COMPUTERCRAFT)) {
            ModPeripheralProvider.register()
        }
    }
}
