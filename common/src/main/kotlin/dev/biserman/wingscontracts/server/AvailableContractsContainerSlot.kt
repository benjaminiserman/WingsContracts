package dev.biserman.wingscontracts.server

import net.minecraft.world.Container
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class AvailableContractsContainerSlot(container: Container, index: Int, x: Int, y: Int) : Slot(container, index, x, y) {
    override fun onTake(player: Player, itemStack: ItemStack) {
        if (!itemStack.isEmpty) {
            AvailableContractsData.setRemainingPicks(player, AvailableContractsData.remainingPicks(player) - 1)
        }
    }

    override fun mayPickup(player: Player): Boolean = AvailableContractsData.remainingPicks(player) > 0
    override fun mayPlace(itemStack: ItemStack): Boolean = false
}