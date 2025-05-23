package dev.biserman.wingscontracts.forge

import dev.biserman.wingscontracts.core.Contract
import dev.biserman.wingscontracts.registry.ModItemRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.ItemStack
import net.minecraftforge.client.IItemDecorator
import net.minecraftforge.client.event.RegisterItemDecorationsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

class ContractItemDecorator : IItemDecorator {
    override fun render(
        graphics: GuiGraphics, font: Font, itemStack: ItemStack, x: Int, y: Int
    ): Boolean {
        val minecraft = Minecraft.getInstance()
        val showItem = Contract.getDisplayItem(itemStack, minecraft.level?.gameTime?.toFloat() ?: 0f)
        if (showItem.isEmpty) {
            return false
        }

        val poseStack = graphics.pose()
        poseStack.pushPose()
        poseStack.translate(
            (x + 4).toFloat(), (y + 4).toFloat(), 100f
        )
        poseStack.scale(0.5f, 0.5f, 0.5f)
        graphics.renderItem(showItem, 0, 0)
        poseStack.popPose()

        return false
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    companion object {
        val instance = ContractItemDecorator()

        @JvmStatic
        @SubscribeEvent
        fun registerItemDecorations(event: RegisterItemDecorationsEvent) {
            event.register(ModItemRegistry.ABYSSAL_CONTRACT.get()!!, instance)
            event.register(ModItemRegistry.BOUND_CONTRACT.get()!!, instance)
        }
    }
}