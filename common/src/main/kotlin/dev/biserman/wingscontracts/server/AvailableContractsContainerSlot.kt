package dev.biserman.wingscontracts.server

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.data.LoadedContracts
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class AvailableContractsContainerSlot(container: Container, index: Int, x: Int, y: Int) : Slot(container, index, x, y) {
    override fun onTake(player: Player, itemStack: ItemStack) {
        WingsContractsMod.LOGGER.info("${player.name.string} took contract ${LoadedContracts[itemStack]?.name}")

        if (!player.isCreative) {
            AvailableContractsData.setRemainingPicks(player, AvailableContractsData.remainingPicks(player) - 1)
        }
    }

    override fun mayPickup(player: Player): Boolean =
        player.isCreative || AvailableContractsData.remainingPicks(player) > 0

    override fun mayPlace(itemStack: ItemStack): Boolean = false
}