package dev.biserman.wingscontracts.item

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.data.ContractDataReloadListener
import dev.biserman.wingscontracts.data.ContractSavedData
import dev.biserman.wingscontracts.registry.ModSoundRegistry
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class BlankAbyssalContractItem(properties: Properties) : Item(properties) {
    override fun use(
        level: Level,
        player: Player,
        interactionHand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        val itemStack = player.getItemInHand(interactionHand)
        if (ModConfig.SERVER.allowBlankAbyssalContractUse.get()) {
            if (level !is ServerLevel) {
                player.playSound(ModSoundRegistry.WRITE_CONTRACT.get())
                return InteractionResultHolder.success(itemStack)
            }

            val contract =
                ContractSavedData.get(level).generator.generateContract(ContractDataReloadListener.randomTag())
            contract.initialize()
            val newContractStack = contract.createItem()
            player.setItemInHand(interactionHand, newContractStack)

            return InteractionResultHolder.success(newContractStack)
        }

        return InteractionResultHolder.pass(itemStack)
    }

    override fun appendHoverText(
        itemStack: ItemStack,
        tooltipContext: TooltipContext,
        components: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        if (ModConfig.SERVER.allowBlankAbyssalContractUse.get()) {
            components.add(
                Component
                    .translatable("item.${WingsContractsMod.MOD_ID}.blank_abyssal_contract.desc.can")
                    .withStyle(ChatFormatting.GRAY)
            )
        } else {
            components.add(
                Component
                    .translatable("item.${WingsContractsMod.MOD_ID}.blank_abyssal_contract.desc.cannot")
                    .withStyle(ChatFormatting.GRAY)
            )
        }
    }
}