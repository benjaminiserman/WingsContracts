package dev.biserman.wingscontracts

import dev.architectury.platform.forge.EventBuses
import dev.biserman.wingscontracts.compat.CompatMods
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.MinecraftForge
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

        val modBus = MOD_BUS
        EventBuses.registerModEventBus(WingsContractsMod.MOD_ID, modBus)

        WingsContractsMod.init(ForgePlatformHelper())
        ForgeModCompat.init(modBus)

        if (ModList.get().isLoaded(CompatMods.COMPUTERCRAFT)) {
            ModPeripheralProvider.register()
        }

        MinecraftForge.EVENT_BUS.addGenericListener(
            BlockEntity::class.java,
            PortalItemHandlerCapabilityProvider::attachCapabilities
        )
    }
}
