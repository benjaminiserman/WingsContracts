package dev.biserman.wingscontracts.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.biserman.wingscontracts.api.Contract
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.level.LightLayer

class ContractPortalBlockEntityRenderer(private val context: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<ContractPortalBlockEntity> {
    override fun render(
        blockEntity: ContractPortalBlockEntity, partialTick: Float, poseStack: PoseStack,
        multiBufferSource: MultiBufferSource, packedLight: Int, packedOverlay: Int
    ) {
        val level = blockEntity.level ?: return

        val blockPos = blockEntity.blockPos.above()
        val relativeGameTime = level.gameTime + partialTick
        val rotation = relativeGameTime * 2

        val showItem = Contract.getDisplayItem(blockEntity.contractSlot, relativeGameTime)
        if (showItem.isEmpty) {
            return
        }

        poseStack.pushPose()
        poseStack.translate(0.5, 1.6, 0.5)
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation))
        context.itemRenderer.renderStatic(
            showItem, ItemDisplayContext.GROUND,
            LightTexture.pack(
                level.getBrightness(LightLayer.BLOCK, blockPos),
                level.getBrightness(LightLayer.SKY, blockPos)
            ),
            OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, level, 0
        )

        poseStack.popPose()
    }
}
