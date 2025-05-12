package dev.biserman.wingscontracts.gui

import dev.biserman.wingscontracts.WingsContractsMod
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractButton
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory

class BoundContractCreationScreen(menu: BoundContractCreationMenu, inventory: Inventory, title: Component) :
    AbstractContainerScreen<BoundContractCreationMenu>(menu, inventory, title) {

    init {
        imageHeight = IMAGE_HEIGHT
        inventoryLabelY = IMAGE_HEIGHT - 94
    }

    val nameBox by lazy {
        EditBox(
            font,
            leftPos + 30,
            topPos + 81,
            90,
            12,
            CommonComponents.EMPTY
        )
    }

    @Suppress("UsePropertyAccessSyntax")
    override fun init() {
        super.init()
        nameBox.setCanLoseFocus(false)
        nameBox.setTextColor(-1)
        nameBox.setTextColorUneditable(-1)
        nameBox.setBordered(false)
        nameBox.setMaxLength(50)
        nameBox.setValue("")
        addWidget(nameBox)
        setInitialFocus(nameBox)

        addRenderableWidget(
            ConfirmButton(leftPos + 135, topPos + 77)
        )
    }

    override fun render(graphics: GuiGraphics, x: Int, y: Int, partialTick: Float) {
        renderBackground(graphics)
        super.render(graphics, x, y, partialTick)
        nameBox.render(graphics, x, y, partialTick)

        renderTooltip(graphics, x, y)
    }

    override fun renderBg(graphics: GuiGraphics, f: Float, i: Int, j: Int) {
        val k = (width - IMAGE_WIDTH) / 2
        val l = (height - IMAGE_HEIGHT) / 2
        graphics.blit(TEXTURE, k, l, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)
    }

    override fun keyPressed(key: Int, mouseX: Int, mouseY: Int): Boolean {
        if (key == 256) {
            Minecraft.getInstance().player!!.closeContainer()
        }

        return nameBox.keyPressed(key, mouseX, mouseY)
                || nameBox.canConsumeInput()
                || super.keyPressed(key, mouseX, mouseY)
    }

    companion object {
        val TEXTURE: ResourceLocation =
            ResourceLocation(WingsContractsMod.MOD_ID, "textures/gui/bound_contract_creation.png")
        const val IMAGE_WIDTH = 176
        const val IMAGE_HEIGHT = 189
    }

    class ConfirmButton(x: Int, y: Int) :
        AbstractButton(x, y, 16, 16, CommonComponents.EMPTY) {

        override fun onPress() {
//            this@BeaconScreen.minecraft.getConnection().send(
//                ServerboundSetBeaconPacket(
//                    Optional.ofNullable<MobEffect?>(this@BeaconScreen.primary),
//                    Optional.ofNullable<MobEffect?>(this@BeaconScreen.secondary)
//                )
//            )
            Minecraft.getInstance().player?.closeContainer()
        }

        override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, f: Float) {
            val imageX = if (isHoveredOrFocused) width else 0

            graphics.blit(TEXTURE, x, y, imageX, IMAGE_Y, width, height)
        }

        override fun updateWidgetNarration(output: NarrationElementOutput) {
            defaultButtonNarrationText(output)
        }

        companion object {
            const val IMAGE_Y = IMAGE_HEIGHT
        }
    }
}