package dev.biserman.wingscontracts.gui

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.server.AvailableContractsData
import dev.biserman.wingscontracts.util.DenominationsHelper
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory

class AvailableContractsScreen(menu: AvailableContractsMenu, val inventory: Inventory, title: Component) :
    AbstractContainerScreen<AvailableContractsMenu>(menu, inventory, title) {

    init {
        imageHeight = 151
        inventoryLabelY = imageHeight - 94
    }

    override fun render(graphics: GuiGraphics, x: Int, y: Int, partialTick: Float) {
        this.renderBackground(graphics)
        super.render(graphics, x, y, partialTick)

        val timeTilRefresh = DenominationsHelper.denominate(
            System.currentTimeMillis() - AvailableContractsData.get(inventory.player.level()).nextCycleStart,
            DenominationsHelper.timeDenominations
        ).asSequence().joinToString(":")
        graphics.drawString(font, "Refreshes in: $timeTilRefresh", titleLabelX + 100, titleLabelY, 0x404040, false)
        this.renderTooltip(graphics, x, y)
    }

    override fun renderBg(graphics: GuiGraphics, f: Float, i: Int, j: Int) {
        val k = (this.width - this.imageWidth) / 2
        val l = (this.height - this.imageHeight) / 2
        graphics.blit(TEXTURE, k, l, 0, 0, this.imageWidth, this.imageHeight)
    }

    companion object {
        val TEXTURE: ResourceLocation = ResourceLocation(WingsContractsMod.MOD_ID, "textures/gui/contract_portal.png")
    }
}