package dev.biserman.wingscontracts.compat.computercraft.peripherals

import dan200.computercraft.api.detail.VanillaDetailRegistries
import net.minecraft.world.item.ItemStack

object DetailsHelper {
    val (ItemStack).details: Map<String, Any> get() = VanillaDetailRegistries.ITEM_STACK.getDetails(this)
}