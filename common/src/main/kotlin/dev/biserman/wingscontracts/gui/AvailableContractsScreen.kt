package dev.biserman.wingscontracts.gui

import com.mojang.blaze3d.systems.RenderSystem
import dev.biserman.wingscontracts.WingsContractsMod
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory

class AvailableContractsScreen(menu: AvailableContractsMenu, inventory: Inventory, title: Component) :
    AbstractContainerScreen<AvailableContractsMenu>(menu, inventory, title) {

    init {
        leftPos = 0
        topPos = 0
        imageWidth = 100
        imageHeight = 100
    }

    override fun renderBg(
        guiGraphics: GuiGraphics,
        partialTicks: Float,
        mouseX: Int,
        mouseY: Int
    ) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.setShaderTexture(0, TEXTURE)
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight)
    }

    companion object {
        val TEXTURE: ResourceLocation = ResourceLocation(WingsContractsMod.MOD_ID, "textures/gui/contract_portal.png")
    }
}