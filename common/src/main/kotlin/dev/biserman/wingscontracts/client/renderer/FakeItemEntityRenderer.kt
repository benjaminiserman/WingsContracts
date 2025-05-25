package dev.biserman.wingscontracts.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.entity.FakeItemEntity
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.ItemEntityRenderer
import net.minecraft.world.entity.item.ItemEntity

class FakeItemEntityRenderer(context: EntityRendererProvider.Context) : ItemEntityRenderer(context) {
    override fun render(
        itemEntity: ItemEntity,
        f: Float,
        g: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        i: Int
    ) {
        WingsContractsMod.LOGGER.info("I'm rendering!!!!!")
        if (itemEntity is FakeItemEntity) {
            WingsContractsMod.LOGGER.info("SUCCESSFULLY")
            super.render(itemEntity.realItemEntity, f, g, poseStack, multiBufferSource, i)
        } else {
            super.render(itemEntity, f, g, poseStack, multiBufferSource, i)
        }
    }
}