package dev.biserman.wingscontracts.gui

import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.registry.ModMenuRegistry
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.inventory.Slot
import net.minecraft.world.inventory.TransientCraftingContainer
import net.minecraft.world.item.ItemStack

class BoundContractCreationMenu(id: Int, inventory: Inventory) :
    AbstractContainerMenu(ModMenuRegistry.BOUND_CONTRACT_CREATION.get(), id) {

    val leftSlots: CraftingContainer = TransientCraftingContainer(this, 3, 3);
    val rightSlots: CraftingContainer = TransientCraftingContainer(this, 3, 3);

    init {
        val maxOptions = ModConfig.SERVER.availableContractsPoolOptions.get()

        for (i in 0..2) {
            for (j in 0..2) {
                this.addSlot(Slot(leftSlots, j + i * 3, 26 + j * 18, 18 + i * 18))
            }
        }

        for (i in 0..2) {
            for (j in 0..2) {
                this.addSlot(Slot(rightSlots, j + i * 3, 98 + j * 18, 18 + i * 18))
            }
        }


        for (i in 0..<3) {
            for (j in 0..<9) {
                this.addSlot(Slot(inventory, j + i * 9 + 9, 8 + j * 18, i * 18 + 107))
            }
        }

        for (i in 0..<9) {
            this.addSlot(Slot(inventory, i, 8 + i * 18, 165))
        }
    }

    override fun quickMoveStack(
        player: Player,
        i: Int
    ): ItemStack = ItemStack.EMPTY

    override fun stillValid(player: Player): Boolean = true
}