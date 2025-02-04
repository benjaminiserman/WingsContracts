package dev.biserman.wingscontracts.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import dev.biserman.wingscontracts.item.ContractItem
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.LightLayer

class ContractPortalBlockEntityRenderer(private val context: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<ContractPortalBlockEntity> {
    override fun render(
        blockEntity: ContractPortalBlockEntity, partialTick: Float, poseStack: PoseStack,
        multiBufferSource: MultiBufferSource, packedLight: Int, packedOverlay: Int
    ) {
        poseStack.pushPose()

        val contractSlot = blockEntity.contractSlot

        if (contractSlot.item !is ContractItem) {
            poseStack.popPose()
            return
        }

        val showItem: ItemStack? = ContractItem.targetItem(contractSlot)
        if (showItem == null) {
            poseStack.popPose()
            return
        }

        val level = blockEntity.level
        if (level == null) {
            poseStack.popPose()
            return
        }

        val blockPos = blockEntity.blockPos.above()
        val relativeGameTime = level.gameTime + partialTick
        val rotation = relativeGameTime * 2

        poseStack.translate(0.5, 1.3, 0.5)
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation))
        context.itemRenderer.renderStatic(
            showItem, ItemDisplayContext.GROUND,
            LightTexture.pack(
                level.getBrightness(LightLayer.BLOCK, blockPos),
                level.getBrightness(LightLayer.SKY, blockPos)
            ),
            OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, level, 0
        )

        // Font font = this.context.getFont();
        // poseStack.scale(0.05f, -0.05f, 0.05f);
        // poseStack.translate(-10f + font.width("hello world") / 2f, -15.0f, 0.0f);
        // font.drawInBatch(
        //         "items:" + itemStack.getDisplayName().getString() + ", " + ContractItem.targetItem(itemStack),
        //         0,
        //         0,
        //         0xECECEC,
        //         false,
        //         poseStack.last().pose(),
        //         multiBufferSource,
        //         Font.DisplayMode.NORMAL,
        //         0,
        //         packedLight);
        poseStack.popPose()
    }
}
