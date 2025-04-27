package dev.biserman.wingscontracts.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.item.ContractItem
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Item

object ModItemRegistry {
    val ITEMS: DeferredRegister<Item?> = DeferredRegister.create(
        WingsContractsMod.MOD_ID,
        Registries.ITEM
    )

    val ABYSSAL_CONTRACT: RegistrySupplier<Item?> = ITEMS.register(
        "abyssal_contract"
    ) { ContractItem(Item.Properties().stacksTo(1)) }

    val QUESTION_MARK: RegistrySupplier<Item?> = ITEMS.register(
        "question_mark"
    ) { Item(Item.Properties().stacksTo(16)) }

    @JvmStatic
    fun register() {
        ITEMS.register()
    }
}
