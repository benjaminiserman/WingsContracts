package dev.biserman.wingscontracts.container

import dev.architectury.networking.NetworkManager
import dev.biserman.wingscontracts.data.ContractDataReloadListener
import dev.biserman.wingscontracts.data.ContractSavedData
import dev.biserman.wingscontracts.server.SyncAvailableContractsPacket
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
            ContractSavedData.setRemainingPicks(player, ContractSavedData.remainingPicks(player) - 1)
        }

        if (player is ServerPlayer) {
            set(
                ContractSavedData
                    .get(player.level())
                    .generator
                    .generateContract(ContractDataReloadListener.randomTag())
                    .createItem()
            )
            NetworkManager.sendToPlayer(player, SyncAvailableContractsPacket(player.level() as ServerLevel))
        }
    }

    override fun mayPickup(player: Player): Boolean =
        player.isCreative || ContractSavedData.remainingPicks(player) > 0

    override fun mayPlace(itemStack: ItemStack): Boolean = false
}