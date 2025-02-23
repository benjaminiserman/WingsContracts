package dev.biserman.wingscontracts.server

import dev.biserman.wingscontracts.server.AvailableContractsData.Companion.MAX_OPTIONS
import net.minecraft.core.NonNullList
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class AvailableContractsContainer : Container {
    val items: NonNullList<ItemStack> = NonNullList.withSize(MAX_OPTIONS, ItemStack.EMPTY)
    override fun getContainerSize(): Int = MAX_OPTIONS
    override fun isEmpty(): Boolean = items.isEmpty() || items.all { it.isEmpty }
    override fun getItem(i: Int): ItemStack? = items[i]

    override fun removeItem(i: Int, count: Int): ItemStack? {
        val itemStack = ContainerHelper.removeItem(items, i, count)
        if (!itemStack.isEmpty) {
            this.setChanged()
        }

        return itemStack
    }

    override fun removeItemNoUpdate(i: Int): ItemStack? = ContainerHelper.takeItem(items, i)
    override fun setItem(i: Int, itemStack: ItemStack) {
        items[i] = itemStack
    }

    override fun setChanged() {}
    override fun stillValid(player: Player): Boolean = true
    override fun clearContent() = items.clear()
}