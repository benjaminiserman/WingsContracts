package dev.biserman.wingscontracts.forge

import com.mojang.blaze3d.platform.Lighting
import dev.biserman.wingscontracts.api.Contract
import dev.biserman.wingscontracts.registry.ModItemRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraftforge.client.IItemDecorator
import net.minecraftforge.client.event.RegisterItemDecorationsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.joml.Matrix4f

class ContractItemDecorator : IItemDecorator {
    override fun render(
        graphics: GuiGraphics, font: Font, itemStack: ItemStack, x: Int, y: Int
    ): Boolean {
        val minecraft = Minecraft.getInstance()
        val showItem = Contract.getDisplayItem(itemStack, minecraft.deltaFrameTime)
        if (showItem.isEmpty) {
            return false
        }

        val bakedModel: BakedModel =
            minecraft.itemRenderer.getModel(showItem, minecraft.player?.level(), minecraft.player, x + y * 1000)
        graphics.pose().pushPose()
        graphics.pose().translate(
            (x + 8).toFloat(), (y + 8).toFloat(), 150f
        )

        graphics.pose().mulPoseMatrix((Matrix4f()).scaling(1.0f, -1.0f, 1.0f))
        graphics.pose().scale(8.0f, 8.0f, 8.0f)
        val skipBlockLight = !bakedModel.usesBlockLight()
        if (skipBlockLight) {
            Lighting.setupForFlatItems()
        }

        minecraft.itemRenderer.render(
            showItem,
            ItemDisplayContext.GUI,
            false,
            graphics.pose(),
            graphics.bufferSource(),
            15728880,
            OverlayTexture.NO_OVERLAY,
            bakedModel
        )
        graphics.flush()
        if (skipBlockLight) {
            Lighting.setupFor3DItems()
        }

        graphics.pose().popPose()

        return true
    }

    companion object {
        val instance = ContractItemDecorator()

        @SubscribeEvent
        fun registerItemDecorations(event: RegisterItemDecorationsEvent) {
            event.register(ModItemRegistry.CONTRACT.get()!!, instance)
        }
    }
}