package dev.biserman.wingscontracts.compat.computercraft

import dan200.computercraft.api.detail.DetailProvider
import dan200.computercraft.impl.ComputerCraftAPIService
import dev.biserman.wingscontracts.api.AbyssalContract
import dev.biserman.wingscontracts.data.LoadedContracts
import dev.biserman.wingscontracts.item.ContractItem
import net.minecraft.world.item.ItemStack

class ModItemDetailProvider : DetailProvider<ItemStack> {
    override fun provideDetails(details: MutableMap<in String, Any>, itemStack: ItemStack) {
        if (itemStack.item is ContractItem) {
            val contract = LoadedContracts[itemStack] as? AbyssalContract ?: return
            details.putAll(contract.details.map { (key, value) -> Pair(key, value ?: mapOf<String, Any>()) })
        }
    }

    companion object {
        fun register() {
            ComputerCraftAPIService.get().itemStackDetailRegistry.addProvider(ModItemDetailProvider())
        }
    }
}