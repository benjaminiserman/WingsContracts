package dev.biserman.wingscontracts.forge

import dev.architectury.platform.forge.EventBuses
import dev.biserman.wingscontracts.WingsContractsMod
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(WingsContractsMod.MOD_ID)
class WingsContractsModForge {
    init {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(WingsContractsMod.MOD_ID, MOD_BUS)

        // Run our common setup.
        WingsContractsMod.init()
    }
}
