package dev.biserman.wingscontracts.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.entity.FakeItemEntity
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.ItemEntityRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemDisplayContext

class FakeItemEntityRenderer(context: EntityRendererProvider.Context) : ItemEntityRenderer(context) {
    override fun render(
        itemEntity: ItemEntity,
        f: Float,
        frameTime: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        i: Int
    ) {
        if (itemEntity !is FakeItemEntity) {
            return
        }

        poseStack.pushPose()
        val itemStack = itemEntity.realItem
        val seed = if (itemStack.isEmpty) 187 else Item.getId(itemStack.item) + itemStack.damageValue
        this.random.setSeed(seed.toLong())
        val bakedModel =
            this.itemRenderer.getModel(itemStack, itemEntity.level(), null as LivingEntity?, itemEntity.id)
        val isGui3d = bakedModel.isGui3d
        val renderAmount = ItemEntityRenderer.getRenderedAmount(itemStack.count)
        val bounceY = Mth.sin((itemEntity.age.toFloat() + frameTime) / 10.0f + itemEntity.bobOffs) * 0.1f + 0.1f
        val yScale = bakedModel.transforms.getTransform(ItemDisplayContext.GROUND).scale.y()
        poseStack.translate(0.0f, bounceY + 0.25f * yScale, 0.0f)
        val spin = itemEntity.getSpin(frameTime)
        poseStack.mulPose(Axis.YP.rotation(spin))
        val sx = bakedModel.transforms.ground.scale.x()
        val sy = bakedModel.transforms.ground.scale.y()
        val sz = bakedModel.transforms.ground.scale.z()
        if (!isGui3d) {
            val dx = -0.0f * (renderAmount - 1).toFloat() * 0.5f * sx
            val dy = -0.0f * (renderAmount - 1).toFloat() * 0.5f * sy
            val dz = -0.09375f * (renderAmount - 1).toFloat() * 0.5f * sz
            poseStack.translate(dx, dy, dz)
        }

        for (j in 0..<renderAmount) {
            poseStack.pushPose()
            if (j > 0) {
                if (isGui3d) {
                    val dx = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f
                    val dy = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f
                    val dz = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f
                    poseStack.translate(dx, dy, dz)
                } else {
                    val dx = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f
                    val dy = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f
                    poseStack.translate(dx, dy, 0.0f)
                }
            }

            this.itemRenderer.render(
                itemStack,
                ItemDisplayContext.GROUND,
                false,
                poseStack,
                multiBufferSource,
                i,
                OverlayTexture.NO_OVERLAY,
                bakedModel
            )
            poseStack.popPose()
            if (!isGui3d) {
                poseStack.translate(0.0f * sx, 0.0f * sy, 0.09375f * sz)
            }
        }

        poseStack.popPose()
    }
}