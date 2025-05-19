package dev.biserman.wingscontracts.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.item.BlankAbyssalContractItem
import dev.biserman.wingscontracts.item.BlankBoundContractItem
import dev.biserman.wingscontracts.item.ContractItem
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item

object ModItemRegistry {
    val ITEMS: DeferredRegister<Item> = DeferredRegister.create(
        WingsContractsMod.MOD_ID,
        Registries.ITEM
    )

    val creativeTab = CreativeModeTabs.REDSTONE_BLOCKS!!

    val BLANK_ABYSSAL_CONTRACT: RegistrySupplier<Item> = ITEMS.register(
        "blank_abyssal_contract"
    ) { BlankAbyssalContractItem(Item.Properties().stacksTo(1).`arch$tab`(creativeTab)) }
    val ABYSSAL_CONTRACT: RegistrySupplier<Item> = ITEMS.register(
        "abyssal_contract"
    ) { ContractItem(Item.Properties().stacksTo(1)) }
    val BLANK_BOUND_CONTRACT: RegistrySupplier<Item> = ITEMS.register(
        "blank_bound_contract"
    ) { BlankBoundContractItem(Item.Properties().stacksTo(1).`arch$tab`(creativeTab)) }
    val BOUND_CONTRACT: RegistrySupplier<Item> = ITEMS.register(
        "bound_contract"
    ) { ContractItem(Item.Properties().stacksTo(1)) }


    val ABYSSAL_COIN: RegistrySupplier<Item> = ITEMS.register(
        "abyssal_coin"
    ) { Item(Item.Properties().stacksTo(64).`arch$tab`(creativeTab)) }
    val QUESTION_MARK: RegistrySupplier<Item> = ITEMS.register(
        "question_mark"
    ) { Item(Item.Properties().stacksTo(16)) }
    val STAR: RegistrySupplier<Item> = ITEMS.register(
        "star"
    ) { Item(Item.Properties().stacksTo(16)) }

    @JvmStatic
    fun register() {
        ITEMS.register()
    }
}
