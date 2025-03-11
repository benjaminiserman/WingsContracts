package dev.biserman.wingscontracts.item

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.data.LoadedContracts
import dev.biserman.wingscontracts.tag.ContractTagHelper
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import java.util.*
import kotlin.math.roundToInt

class ContractItem(properties: Properties) : Item(properties) {
    // TODO: how do I localize this properly?
    // e.g.: Contract de Niveau 10 des Diamants de winggar
    override fun getName(itemStack: ItemStack): Component {
        val contract = LoadedContracts[itemStack]
            ?: return Component.translatable("item.${WingsContractsMod.MOD_ID}.contract.unknown")
        return contract.displayName
    }

    override fun appendHoverText(
        itemStack: ItemStack,
        level: Level?,
        components: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        val contract = LoadedContracts[itemStack]
        if (contract == null) {
            components.add(Component.translatable("item.${WingsContractsMod.MOD_ID}.contract.unknown"))
            return
        }

        components.addAll(
            contract.getDescription(
                Screen.hasShiftDown(),
                Component.translatable("${WingsContractsMod.MOD_ID}.hold_shift").string
            )
        )
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

    override fun getBarWidth(itemStack: ItemStack): Int {
        val unitsFulfilled = LoadedContracts[itemStack]?.unitsFulfilled?.toFloat() ?: return 0
        val unitsDemanded = LoadedContracts[itemStack]?.unitsDemanded?.toFloat() ?: return 0

        return (unitsFulfilled * 13.0f / unitsDemanded).roundToInt()
    }

    override fun getBarColor(itemStack: ItemStack): Int = Mth.hsvToRgb(21f / 36f, 1.0f, 1.0f)

    override fun getTooltipImage(itemStack: ItemStack): Optional<TooltipComponent> {
        return Optional.of(ContractTooltip(itemStack))
    }
}
