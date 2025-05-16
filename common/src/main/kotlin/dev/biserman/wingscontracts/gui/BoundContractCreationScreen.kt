package dev.biserman.wingscontracts.gui

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.gui.BoundContractCreationScreen.ScrollField.Companion.BUTTON_IMAGE_Y
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractButton
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Suppress("UsePropertyAccessSyntax")
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

    val leftScrollField by lazy { ScrollField(leftPos + 4, topPos + 28, font) }
    val rightScrollField by lazy { ScrollField(leftPos + 154, topPos + 28, font) }

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

        for (widget in leftScrollField.widgets) {
            addRenderableWidget(widget)
        }

        for (widget in rightScrollField.widgets) {
            addRenderableWidget(widget)
        }
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

    inner class ConfirmButton(x: Int, y: Int) :
        AbstractButton(x, y, 16, 16, CommonComponents.EMPTY) {

        override fun onPress() {
            if (menu.isValidContract(leftScrollField.scrollValue, rightScrollField.scrollValue)) {
                menu.submit(leftScrollField.scrollValue, rightScrollField.scrollValue, nameBox.value)
                Minecraft.getInstance().player?.closeContainer()
            }
        }

        override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, f: Float) {
            val imageX = when {
                !menu.isValidContract(leftScrollField.scrollValue, rightScrollField.scrollValue) -> width * 2
                isHovered -> width
                else -> 0
            }

            graphics.blit(TEXTURE, x, y, imageX, BUTTON_IMAGE_Y, width, height)
        }

        override fun updateWidgetNarration(output: NarrationElementOutput) {
            defaultButtonNarrationText(output)
        }
    }

    class ScrollField(x: Int, y: Int, font: Font) :
        AbstractWidget(x, y, BUTTON_WIDTH + 8, BUTTON_HEIGHT * 2 + TEXT_HEIGHT + 8, CommonComponents.EMPTY) {
        var scrollValue = 1
            set(value) {
                field = min(max(1, value), 1024)
                textField.message = Component.literal(field.toString().padStart((4 - field.toString().length) / 2))
            }

        val upButton = ScrollButton(x + 4, y + 4, +1)
        val downButton = ScrollButton(x + 4, y + BUTTON_HEIGHT + TEXT_HEIGHT + 4, -1)
        val textField = StringWidget(
            x + 1,
            y + BUTTON_HEIGHT + 4,
            BUTTON_WIDTH + 6,
            TEXT_HEIGHT,
            CommonComponents.EMPTY,
            font
        ).alignCenter()!!
        val widgets = listOf(this, upButton, downButton, textField)

        init {
            textField.message = Component.literal("1")
        }

        override fun mouseClicked(d: Double, e: Double, i: Int): Boolean {
            return super.mouseClicked(d, e, i)
        }

        override fun renderWidget(guiGraphics: GuiGraphics, i: Int, j: Int, f: Float) {}

        override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}

        override fun mouseScrolled(d: Double, e: Double, f: Double): Boolean {
            val trueDelta = if (hasShiftDown()) f.roundToInt() * 10 else f.roundToInt()
            scrollValue += trueDelta
            return true
        }

        override fun onClick(x: Double, y: Double) {
            upButton.mouseClicked(x, y, 0)
            downButton.mouseClicked(x, y, 0)
        }

        inner class ScrollButton(x: Int, y: Int, val delta: Int) :
            AbstractButton(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, CommonComponents.EMPTY) {

            override fun onPress() {
                val trueDelta = if (hasShiftDown()) delta * 10 else delta
                scrollValue += trueDelta
            }

            override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, f: Float) {
                val imageX = when {
                    isHovered -> width
                    else -> 0
                }

                graphics.blit(TEXTURE, x, y, imageX, if (delta > 0) IMAGE_Y else IMAGE_Y + BUTTON_HEIGHT, width, height)
            }

            override fun updateWidgetNarration(output: NarrationElementOutput) {
                defaultButtonNarrationText(output)
            }
        }

        companion object {
            const val BUTTON_IMAGE_Y = IMAGE_HEIGHT
            const val IMAGE_Y = IMAGE_HEIGHT + 16
            const val BUTTON_WIDTH = 9
            const val BUTTON_HEIGHT = 6
            const val TEXT_HEIGHT = 12
        }
    }

}