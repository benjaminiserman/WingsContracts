package dev.biserman.wingscontracts.fabric

import dev.biserman.wingscontracts.WingsContractsMod
import net.fabricmc.api.ModInitializer

object WingsContractsModFabric : ModInitializer {
    override fun onInitialize() {
        WingsContractsMod.init()
    }
}
