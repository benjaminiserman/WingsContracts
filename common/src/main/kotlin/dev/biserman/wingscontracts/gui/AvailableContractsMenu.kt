package dev.biserman.wingscontracts.gui

import dev.biserman.wingscontracts.registry.ModMenuRegistry
import dev.biserman.wingscontracts.server.AvailableContractsContainer
import dev.biserman.wingscontracts.server.AvailableContractsData
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

@OptIn(ExperimentalStdlibApi::class)
class AvailableContractsMenu(id: Int, inventory: Inventory) :
    AbstractContainerMenu(ModMenuRegistry.CONTRACT_PORTAL.get(), id) {

    val container: AvailableContractsContainer =
        AvailableContractsData.get(Minecraft.getInstance().level!!)!!.container // this will always fail :/

    init {
        checkContainerSize(container, AvailableContractsData.MAX_OPTIONS)
        container.startOpen(inventory.player)
        for (i in 0..<AvailableContractsData.MAX_OPTIONS) {
            this.addSlot(Slot(container, i, 44 + i * 18, 20))
        }

        for (i in 0..<3) {
            for (j in 0..<9) {
                this.addSlot(Slot(inventory, j + i * 9 + 9, 8 + j * 18, i * 18 + 51))
            }
        }

        for (i in 0..<9) {
            this.addSlot(Slot(inventory, i, 8 + i * 18, 109))
        }
    }

    override fun quickMoveStack(
        player: Player,
        i: Int
    ): ItemStack? {
        val slot = slots[i]
        if (!slot.hasItem()) {
            return ItemStack.EMPTY
        }

        val slotItemStack = slot.item
        val itemStack = slotItemStack.copy()
        if (i < container.containerSize) {
            if (!this.moveItemStackTo(slotItemStack, container.containerSize, slots.size, true)) {
                return ItemStack.EMPTY
            }
        } else {
            return ItemStack.EMPTY
        }

        if (slotItemStack.isEmpty) {
            slot.setByPlayer(ItemStack.EMPTY)
        } else {
            slot.setChanged()
        }

        return itemStack
    }

    override fun stillValid(player: Player): Boolean = container.stillValid(player)
}