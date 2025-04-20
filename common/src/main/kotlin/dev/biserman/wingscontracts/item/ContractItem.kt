package dev.biserman.wingscontracts.item

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.data.LoadedContracts
import dev.biserman.wingscontracts.server.AvailableContractsData
import dev.biserman.wingscontracts.tag.ContractTagHelper
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import kotlin.math.ceil

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

        val holdShift =
            Component.translatable("${WingsContractsMod.MOD_ID}.hold_shift_1")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(
                    Component.translatable("${WingsContractsMod.MOD_ID}.shift")
                        .withStyle(ChatFormatting.GRAY)
                ).append(
                    Component.translatable("${WingsContractsMod.MOD_ID}.hold_shift_2")
                        .withStyle(ChatFormatting.DARK_GRAY)
                )

        components.addAll(contract.getDescription(Screen.hasShiftDown(), holdShift))
    }

    override fun getRarity(itemStack: ItemStack): Rarity {
        val contract = LoadedContracts[itemStack] ?: return super.getRarity(itemStack)
        return Rarity.values()[contract.getRarity()]
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

    override fun inventoryTick(itemStack: ItemStack, level: Level, entity: Entity, i: Int, bl: Boolean) {
        LoadedContracts[itemStack]?.tryUpdateTick(ContractTagHelper.getContractTag(itemStack))
    }

    override fun isBarVisible(itemStack: ItemStack): Boolean {
        val contract = LoadedContracts[itemStack] ?: return false
        val isZeroWidth = getBarWidth(itemStack) == 0
        val isExpired = System.currentTimeMillis() < contract.currentCycleStart + contract.cycleDurationMs
        return !isZeroWidth && !isExpired
    }

    override fun getBarWidth(itemStack: ItemStack): Int {
        val unitsFulfilled = LoadedContracts[itemStack]?.unitsFulfilled?.toFloat() ?: 0f
        val unitsDemanded = LoadedContracts[itemStack]?.unitsDemanded?.toFloat() ?: 0f

        if (unitsDemanded == 0f || unitsFulfilled == 0f) {
            return 0
        }

        return ceil(unitsFulfilled * 13.0f / unitsDemanded).toInt()
    }

    override fun onCraftedBy(itemStack: ItemStack, level: Level, player: Player) {
        if (level is ServerLevel
            && ContractTagHelper.getContractTag(itemStack) == null
            && ModConfig.SERVER.randomizeCraftedContracts.get()
        ) {
            val contract = AvailableContractsData.get(level).generateContract()
            val item = contract.createItem()
            ContractTagHelper.setContractTag(itemStack, ContractTagHelper.getContractTag(item)!!)
        }
    }

    override fun getBarColor(itemStack: ItemStack): Int = 0xff55ff
}
