package dev.biserman.wingscontracts.registry

import dev.architectury.registry.menu.MenuRegistry
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.gui.AvailableContractsMenu
import dev.biserman.wingscontracts.gui.AvailableContractsScreen
import dev.biserman.wingscontracts.gui.BoundContractCreationMenu
import dev.biserman.wingscontracts.gui.BoundContractCreationScreen
import net.minecraft.core.registries.Registries
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.inventory.MenuType

object ModMenuRegistry {
    val MENUS: DeferredRegister<MenuType<*>> = DeferredRegister
        .create(WingsContractsMod.MOD_ID, Registries.MENU)

    val CONTRACT_PORTAL: RegistrySupplier<MenuType<AvailableContractsMenu>> = MENUS
        .register("contract_portal") {
            MenuType(::AvailableContractsMenu, FeatureFlags.REGISTRY.allFlags())
        }

    val BOUND_CONTRACT_CREATION: RegistrySupplier<MenuType<BoundContractCreationMenu>> =
        MENUS.register("bound_contract_creation") {
            MenuType(::BoundContractCreationMenu, FeatureFlags.REGISTRY.allFlags())
        }

    @JvmStatic
    fun register() {
        MENUS.register()
    }

    @JvmStatic
    fun clientsideRegister() {
        MenuRegistry.registerScreenFactory(CONTRACT_PORTAL.get(), ::AvailableContractsScreen)
        MenuRegistry.registerScreenFactory(BOUND_CONTRACT_CREATION.get(), ::BoundContractCreationScreen)
    }
}