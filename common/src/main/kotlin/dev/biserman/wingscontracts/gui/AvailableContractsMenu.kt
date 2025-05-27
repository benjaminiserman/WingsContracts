package dev.biserman.wingscontracts.gui

import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.registry.ModMenuRegistry
import dev.biserman.wingscontracts.container.AvailableContractsContainerSlot
import dev.biserman.wingscontracts.data.ContractSavedData
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class AvailableContractsMenu(id: Int, inventory: Inventory) :
    AbstractContainerMenu(ModMenuRegistry.CONTRACT_PORTAL.get(), id) {

    val data = ContractSavedData.get(inventory.player.level())
    val container = data.container

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
                this.addSlot(
                    AvailableContractsContainerSlot(
                        container,
                        i + topRowCount,
                        44 + 9 * (5 - bottomRowCount) + i * 18,
                        54
                    )
                )
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
    ): ItemStack {
        if (i >= slots.size) {
            return ItemStack.EMPTY
        }

        val slot = slots[i]
        if (slot.hasItem() && slot is AvailableContractsContainerSlot && slot.mayPickup(player)) {
            val itemStack = slot.item
            if (!this.moveItemStackTo(
                    itemStack,
                    ModConfig.SERVER.availableContractsPoolOptions.get(),
                    this.slots.size,
                    true
                )
            ) {
                return ItemStack.EMPTY
            }

            slot.onTake(player, itemStack)
            return itemStack
        }

        return ItemStack.EMPTY
    }

    override fun stillValid(player: Player): Boolean = container.stillValid(player)
}