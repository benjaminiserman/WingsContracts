package dev.biserman.wingscontracts.container

import dev.biserman.wingscontracts.data.AvailableContractsData
import dev.biserman.wingscontracts.data.AvailableContractsManager
import dev.biserman.wingscontracts.server.SyncAvailableContractsMessage
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class AvailableContractsContainerSlot(
    availableContractsContainer: AvailableContractsContainer,
    index: Int,
    x: Int,
    y: Int
) : Slot(availableContractsContainer, index, x, y) {
    override fun onTake(player: Player, itemStack: ItemStack) {
        if (!player.isCreative) {
            AvailableContractsData.setRemainingPicks(player, AvailableContractsData.remainingPicks(player) - 1)
        }

        if (player is ServerPlayer) {
            if (item.isEmpty) {
                set(
                    AvailableContractsData
                        .get(player.level())
                        .generator
                        .generateContract(AvailableContractsManager.randomTag())
                        .createItem()
                )
            }
            SyncAvailableContractsMessage(player.level() as ServerLevel).sendTo(player)
        }
    }

    override fun mayPickup(player: Player): Boolean =
        player.isCreative || AvailableContractsData.remainingPicks(player) > 0

    override fun mayPlace(itemStack: ItemStack): Boolean = false
}