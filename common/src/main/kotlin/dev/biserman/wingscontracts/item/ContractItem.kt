package dev.biserman.wingscontracts.item

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.core.AbyssalContract
import dev.biserman.wingscontracts.core.AbyssalContract.Companion.currentCycleStart
import dev.biserman.wingscontracts.core.Contract.Companion.unitsFulfilledEver
import dev.biserman.wingscontracts.data.LoadedContracts
import dev.biserman.wingscontracts.nbt.ContractTagHelper
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import kotlin.math.ceil

class ContractItem(properties: Properties) : Item(properties) {
    override fun getName(itemStack: ItemStack): Component {
        val contract = LoadedContracts[itemStack]
            ?: return Component.translatable("item.${WingsContractsMod.MOD_ID}.contract.unknown")
        val rarity = itemStack.get(DataComponents.RARITY)?.ordinal ?: 0

        return contract.getDisplayName(rarity)
    }

    override fun appendHoverText(
        itemStack: ItemStack,
        tooltipContext: TooltipContext,
        components: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        val contract = LoadedContracts[itemStack]
        if (contract == null) {
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

    override fun inventoryTick(itemStack: ItemStack, level: Level, entity: Entity, i: Int, bl: Boolean) {
        val contract = LoadedContracts[itemStack] ?: return
        val contractTag = ContractTagHelper.getContractTag(itemStack) ?: return
        if (level is ServerLevel) {
            contract.tryUpdateTick(contractTag)
        } else if (contract.unitsFulfilledEver != contractTag.unitsFulfilledEver
            || (contract is AbyssalContract && contract.currentCycleStart != contractTag.currentCycleStart)) {
            // this is a cheap check to invalidate the cache and avoid certain sync issues
            LoadedContracts.invalidate(contract.id)
        }
    }

    override fun isBarVisible(itemStack: ItemStack): Boolean {
        val contract = LoadedContracts[itemStack] ?: return false

        if (contract is AbyssalContract) {
            val isZeroWidth = getBarWidth(itemStack) == 0
            val isExpired = System.currentTimeMillis() > contract.currentCycleStart + contract.cycleDurationMs
            return !isZeroWidth && !isExpired
        } else {
            return false
        }
    }

    override fun getBarWidth(itemStack: ItemStack): Int {
        val contract = LoadedContracts[itemStack] ?: return 0
        if (contract is AbyssalContract) {
            val unitsFulfilled = contract.unitsFulfilled.toFloat()
            val unitsDemanded = contract.unitsDemanded.toFloat()

            return ceil(unitsFulfilled * 13.0f / unitsDemanded).toInt()
        } else {
            return 0
        }
    }

    override fun getBarColor(itemStack: ItemStack): Int = 0xff55ff
}
