package dev.biserman.wingscontracts.registry

import dev.architectury.registry.ReloadListenerRegistry
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.data.AvailableContractsManager
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType

object ModReloadListenerRegistry {
    fun register() {
        ReloadListenerRegistry.register(
            PackType.SERVER_DATA, AvailableContractsManager, ResourceLocation(
                WingsContractsMod.MOD_ID, "available_contracts"
            )
        )
    }
}