package dev.biserman.wingscontracts.item

import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.data.AvailableContractsManager
import dev.biserman.wingscontracts.registry.ModSoundRegistry
import dev.biserman.wingscontracts.server.AvailableContractsData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class BlankAbyssalContract(properties: Properties) : Item(properties) {
    override fun use(
        level: Level,
        player: Player,
        interactionHand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        val itemStack = player.getItemInHand(interactionHand)
        if (ModConfig.SERVER.allowBlankContractInitialization.get()
        ) {
            if (level is ServerLevel) {
                return InteractionResultHolder.success(itemStack)
            }

            val contract = AvailableContractsData.get(level).generateContract(AvailableContractsManager.randomTag())
            val newContractStack = contract.createItem()
            player.setItemInHand(interactionHand, newContractStack)
            player.playSound(ModSoundRegistry.WRITE_CONTRACT.get())

            return InteractionResultHolder.success(newContractStack)
        }

        return InteractionResultHolder.pass(itemStack)
    }
}