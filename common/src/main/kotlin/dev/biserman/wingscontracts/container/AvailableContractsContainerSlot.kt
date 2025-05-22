package dev.biserman.wingscontracts.container

import dev.biserman.wingscontracts.data.AvailableContractsManager
import dev.biserman.wingscontracts.server.AvailableContractsData
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
            AvailableContractsData.Companion.setRemainingPicks(player, AvailableContractsData.Companion.remainingPicks(player) - 1)
        }

        if (player is ServerPlayer) {
            if (item.isEmpty) {
                set(
                    AvailableContractsData.Companion.get(player.level())
                        .generateContract(AvailableContractsManager.randomTag())
                        .createItem()
                )
            }
            SyncAvailableContractsMessage(player.level() as ServerLevel).sendTo(player)
        }
    }

    override fun mayPickup(player: Player): Boolean =
        player.isCreative || AvailableContractsData.Companion.remainingPicks(player) > 0

    override fun mayPlace(itemStack: ItemStack): Boolean = false
}