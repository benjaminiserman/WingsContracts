package dev.biserman.wingscontracts.gui

import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.registry.ModMenuRegistry
import dev.biserman.wingscontracts.server.AvailableContractsContainer
import dev.biserman.wingscontracts.server.AvailableContractsContainerSlot
import dev.biserman.wingscontracts.server.AvailableContractsData
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

@OptIn(ExperimentalStdlibApi::class)
class AvailableContractsMenu(id: Int, inventory: Inventory) :
    AbstractContainerMenu(ModMenuRegistry.CONTRACT_PORTAL.get(), id) {

    val container: AvailableContractsContainer =
        AvailableContractsData.get(inventory.player.level()).container

    init {
        val maxOptions = ModConfig.SERVER.availableContractsPoolOptions.get()
        checkContainerSize(container, maxOptions)
        container.startOpen(inventory.player)

        if (maxOptions > 5) {
            val bottomRowCount = maxOptions / 2
            val topRowCount = maxOptions - bottomRowCount
            for (i in 0..<topRowCount) {
                this.addSlot(AvailableContractsContainerSlot(container, i, 44 + 9 * (5 - topRowCount) + i * 18, 36))
            }
            for (i in 0..<bottomRowCount) {
                this.addSlot(AvailableContractsContainerSlot(container, i + topRowCount, 44 + 9 * (5 - bottomRowCount) + i * 18, 54))
            }
        } else {
            for (i in 0..<maxOptions) {
                this.addSlot(AvailableContractsContainerSlot(container, i, 44 + i * 18, 45))
            }
        }

        for (i in 0..<3) {
            for (j in 0..<9) {
                this.addSlot(Slot(inventory, j + i * 9 + 9, 8 + j * 18, i * 18 + 85))
            }
        }

        for (i in 0..<9) {
            this.addSlot(Slot(inventory, i, 8 + i * 18, 143))
        }
    }

    override fun quickMoveStack(
        player: Player,
        i: Int
    ): ItemStack? {
        val slot = slots[i]
        if (!slot.hasItem() || !slot.mayPickup(player)) {
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
            slot.onTake(player, itemStack)
        } else {
            slot.setChanged()
        }

        return itemStack
    }

    override fun stillValid(player: Player): Boolean = container.stillValid(player)
}