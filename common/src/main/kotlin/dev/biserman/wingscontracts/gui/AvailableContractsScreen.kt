package dev.biserman.wingscontracts.gui

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.config.ModConfig
import dev.biserman.wingscontracts.data.ContractSavedData
import dev.biserman.wingscontracts.util.DenominationsHelper
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory

class AvailableContractsScreen(menu: AvailableContractsMenu, val inventory: Inventory, title: Component) :
    AbstractContainerScreen<AvailableContractsMenu>(menu, inventory, title) {

    init {
        imageHeight = 167
        inventoryLabelY = imageHeight - 94
    }

    override fun render(graphics: GuiGraphics, x: Int, y: Int, partialTick: Float) {
        this.renderBackground(graphics, x, y, partialTick)
        super.render(graphics, x, y, partialTick)

        this.renderTooltip(graphics, x, y)
    }

    fun pad2(count: Int?) = (count ?: 0).toString().padStart(2, '0')

    override fun renderLabels(graphics: GuiGraphics, i: Int, j: Int) {
        val shortTitle = Component.translatable("${WingsContractsMod.MOD_ID}.gui.contract_portal.short_title")
        graphics.drawString(font, shortTitle, titleLabelX, titleLabelY, 4210752, false)
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 4210752, false)

        val timeTilRefreshUnits = DenominationsHelper.denominate(
            ContractSavedData.get(inventory.player.level()).nextCycleStart - System.currentTimeMillis(),
            DenominationsHelper.timeDenominationsWithoutMs
        ).asSequence().map { it.first.key to it.second }.toMap()

        val timeTilRefresh = timeTilRefreshUnits.run {
            when {
                "day" in timeTilRefreshUnits
                    -> "${this["day"]}:${pad2(this["hour"])}:${pad2(this["minute"])}:${pad2(this["second"])}"
                "hour" in timeTilRefreshUnits
                    -> "${this["hour"]}:${pad2(this["minute"])}:${pad2(this["second"])}"
                "minute" in timeTilRefreshUnits
                    -> "${this["minute"]}:${pad2(this["second"])}"
                "second" in timeTilRefreshUnits
                    -> this["second"].toString()
                else -> "0"
            }
        }

        val rightTitleEdge = imageWidth - titleLabelY
        val remainingPicksLabel = Component.translatable(
            "${WingsContractsMod.MOD_ID}.gui.contract_portal.remaining_picks",
            if (inventory.player.isCreative) "∞" else ContractSavedData.remainingPicks(inventory.player)
                .toString()
        )
        graphics.drawString(
            font,
            remainingPicksLabel,
            rightTitleEdge - font.width(remainingPicksLabel),
            titleLabelY,
            0x404040,
            false
        )

        if (ModConfig.SERVER.abyssalContractsPoolRefreshPeriodMs.get() > 0) {
            val refreshLabel = Component.translatable(
                "${WingsContractsMod.MOD_ID}.gui.contract_portal.refreshes_in",
                timeTilRefresh
            ).string
            graphics.drawString(
                font,
                refreshLabel,
                rightTitleEdge - font.width(refreshLabel),
                titleLabelY + font.lineHeight + 2,
                0x404040,
                false
            )
        }
    }

    override fun renderBg(graphics: GuiGraphics, f: Float, i: Int, j: Int) {
        val k = (this.width - this.imageWidth) / 2
        val l = (this.height - this.imageHeight) / 2
        graphics.blit(TEXTURE, k, l, 0, 0, this.imageWidth, this.imageHeight)
    }

    companion object {
        val TEXTURE: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath(WingsContractsMod.MOD_ID, "textures/gui/contract_portal.png")
    }
}