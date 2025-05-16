package dev.biserman.wingscontracts.item

import dev.architectury.registry.menu.MenuRegistry
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.registry.ModMenuRegistry
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class BlankBoundContractItem(properties: Properties) : Item(properties), MenuProvider {
    override fun use(
        level: Level,
        player: Player,
        interactionHand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        val itemStack = player.getItemInHand(interactionHand)
        if (player is ServerPlayer) {
            MenuRegistry.openMenu(player, this)
            return InteractionResultHolder.consume(itemStack)
        } else {
            return InteractionResultHolder.success(itemStack)
        }
    }

    override fun getDisplayName(): Component =
        Component.translatable("item.${WingsContractsMod.MOD_ID}.blank_bound_contract")

    override fun createMenu(
        i: Int,
        inventory: Inventory,
        player: Player
    ): AbstractContainerMenu = ModMenuRegistry.BOUND_CONTRACT_CREATION.get().create(i, inventory)

    override fun appendHoverText(
        itemStack: ItemStack,
        level: Level?,
        components: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        val lossRate = ModConfig.SERVER.boundContractLossRate.get()
        if (lossRate != 0.0) {
            components.add(
                Component
                    .translatable(
                        "item.${WingsContractsMod.MOD_ID}.blank_bound_contract.desc.loss_rate",
                        lossRate * 100
                    )
                    .withStyle(ChatFormatting.GRAY)
            )
        }
    }
}