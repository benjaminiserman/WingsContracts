package dev.biserman.wingscontracts.client

import dev.biserman.wingscontracts.data.LoadedContracts
import dev.biserman.wingscontracts.item.ContractTooltip
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack

@Environment(EnvType.CLIENT)
class ClientContractTooltip(val tooltip: ContractTooltip) : ClientTooltipComponent {
    override fun getHeight(): Int = 26
    override fun getWidth(font: Font): Int = 20

    override fun renderImage(font: Font, x: Int, y: Int, guiGraphics: GuiGraphics) {
        val contract = LoadedContracts[tooltip.contractItemStack] ?: return
        val relativeGameTime = System.currentTimeMillis()
        val showItem =
            if (contract.allMatchingItems.isEmpty()) ItemStack.EMPTY
            else contract.allMatchingItems[Mth.floor(relativeGameTime / 30.0f) % contract.allMatchingItems.size]

        guiGraphics.renderItem(showItem, 2, 2, 1)
        guiGraphics.renderItemDecorations(font, showItem, 2, 2)
    }
}