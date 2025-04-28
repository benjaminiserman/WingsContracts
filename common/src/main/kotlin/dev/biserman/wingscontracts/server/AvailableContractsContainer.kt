package dev.biserman.wingscontracts.server

import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.data.AvailableContractsManager
import net.minecraft.core.NonNullList
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class AvailableContractsContainer(val data: AvailableContractsData) : Container {
    val items: NonNullList<ItemStack> =
        NonNullList.withSize(ModConfig.SERVER.availableContractsPoolOptions.get(), ItemStack.EMPTY)

    override fun getContainerSize(): Int = ModConfig.SERVER.availableContractsPoolOptions.get()
    override fun isEmpty(): Boolean = items.isEmpty() || items.all { it.isEmpty }
    override fun getItem(i: Int): ItemStack? = items[i]

    override fun removeItem(i: Int, count: Int): ItemStack? {
        val itemStack = ContainerHelper.removeItem(items, i, count)
        if (!itemStack.isEmpty) {
            setItem(i, data.generateContract(AvailableContractsManager.randomTag()).createItem())
            this.setChanged()
        }

        return itemStack
    }

    override fun removeItemNoUpdate(i: Int): ItemStack? {
        val itemStack = ContainerHelper.takeItem(items, i)
        if (!itemStack.isEmpty) {
            setItem(i, data.generateContract(AvailableContractsManager.randomTag()).createItem())
        }

        return itemStack
    }

    override fun setItem(i: Int, itemStack: ItemStack) {
        items[i] = itemStack
    }

    override fun setChanged() {
        data.setDirty()
    }

    override fun stillValid(player: Player): Boolean = true
    override fun clearContent() = items.clear()

    override fun startOpen(player: Player) {
        super.startOpen(player)
    }
}