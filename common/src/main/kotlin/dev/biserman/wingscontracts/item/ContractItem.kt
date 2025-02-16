package dev.biserman.wingscontracts.item

import dev.biserman.wingscontracts.data.LoadedContracts
import dev.biserman.wingscontracts.tag.ContractTagHelper
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class ContractItem(properties: Properties) : Item(properties) {
    // TODO: how do I localize this properly?
    // e.g.: Contract de Niveau 10 des Diamants de winggar
    override fun getName(itemStack: ItemStack): Component {
        val contract = LoadedContracts[itemStack] ?: return Component.literal("Unknown Contract")
        return Component.literal(contract.displayName)
    }

    override fun appendHoverText(
        itemStack: ItemStack,
        level: Level?,
        components: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        val contract = LoadedContracts[itemStack]
        if (contract == null) {
            components.add(Component.literal("Unknown Contract"))
            return
        }

        components.addAll(contract.getDescription(Screen.hasShiftDown(), "Hold shift for more info"))
    }

    override fun use(
        level: Level,
        player: Player,
        interactionHand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        val itemInHand = player.getItemInHand(interactionHand)
        if (itemInHand.item is ContractItem) {
            LoadedContracts[itemInHand]?.tryUpdateTick(ContractTagHelper.getContractTag(itemInHand))
        }

        return super.use(level, player, interactionHand)
    }
}
