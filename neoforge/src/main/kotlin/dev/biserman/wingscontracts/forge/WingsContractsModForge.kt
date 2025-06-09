package dev.biserman.wingscontracts.neoforge

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.compat.CompatMods
import dev.biserman.wingscontracts.neoforge.block.PortalItemHandlerCapabilityProvider
import dev.biserman.wingscontracts.neoforge.compat.ForgeModCompat
import dev.biserman.wingscontracts.neoforge.compat.ModPeripheralProvider
import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.fml.ModList
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.addGenericListener
import dev.biserman.wingscontracts.config.ModConfig as WingsContractsConfig

@Mod(WingsContractsMod.MOD_ID)
class WingsContractsModForge {
    init {
        @Suppress("removal")
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, WingsContractsConfig.SERVER_SPEC)

        val modBus = MOD_BUS
        EventBuses.registerModEventBus(WingsContractsMod.MOD_ID, modBus)

        WingsContractsMod.init()
        ForgeModCompat.init(modBus)

        if (ModList.get().isLoaded(CompatMods.COMPUTERCRAFT)) {
            ModPeripheralProvider.register()
        }

        NeoForge.EVENT_BUS.addGenericListener(
            BlockEntity::class.java,
            PortalItemHandlerCapabilityProvider::attachCapabilities
        )
    }
}
