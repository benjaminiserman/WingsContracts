package dev.biserman.wingscontracts.neoforge

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.compat.CompatMods
import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.neoforge.block.PortalItemHandlerCapabilityProvider
import dev.biserman.wingscontracts.neoforge.compat.ForgeModCompat
import dev.biserman.wingscontracts.neoforge.compat.ModPeripheralProvider
import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.ModList
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig as NeoForgeConfig
import net.neoforged.neoforge.common.NeoForge

@Mod(WingsContractsMod.MOD_ID)
class WingsContractsModNeoForge(bus: IEventBus, container: ModContainer) {
    init {
        container.registerConfig(NeoForgeConfig.Type.SERVER, ModConfig.SERVER_SPEC)

        WingsContractsMod.init()
        ForgeModCompat.init(bus)

        if (ModList.get().isLoaded(CompatMods.COMPUTERCRAFT)) {
            ModPeripheralProvider.register()
        }

        NeoForge.EVENT_BUS.addGenericListener(
            BlockEntity::class.java,
            PortalItemHandlerCapabilityProvider::attachCapabilities
        )
    }
}
