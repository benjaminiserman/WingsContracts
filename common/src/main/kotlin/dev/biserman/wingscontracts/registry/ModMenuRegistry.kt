package dev.biserman.wingscontracts.registry

import dev.architectury.registry.menu.MenuRegistry
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.gui.AvailableContractsMenu
import dev.biserman.wingscontracts.gui.AvailableContractsScreen
import net.minecraft.core.registries.Registries
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.inventory.MenuType

object ModMenuRegistry {
    val MENUS: DeferredRegister<MenuType<*>> = DeferredRegister
        .create(WingsContractsMod.MOD_ID, Registries.MENU)

    val CONTRACT_PORTAL: RegistrySupplier<MenuType<AvailableContractsMenu>> = MENUS
        .register(
            "contract_portal"
        ) {
            MenuType(::AvailableContractsMenu, FeatureFlags.REGISTRY.allFlags())
        }

    @JvmStatic
    fun register() {
        MENUS.register()
    }

    @JvmStatic
    fun clientsideRegister() {
        MenuRegistry.registerScreenFactory(CONTRACT_PORTAL.get(), ::AvailableContractsScreen)
    }
}