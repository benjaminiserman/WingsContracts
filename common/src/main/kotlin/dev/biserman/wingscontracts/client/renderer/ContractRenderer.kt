package dev.biserman.wingscontracts.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.biserman.wingscontracts.api.Contract
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.LightLayer
import org.joml.Vector3d

object ContractRenderer {
    fun render(
        context: BlockEntityRendererProvider.Context,
        contract: Contract,
        blockEntity: ContractPortalBlockEntity,
        translate: Vector3d,
        partialTick: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource
    ) {
        val level = blockEntity.level ?: return

        val blockPos = blockEntity.blockPos.above()
        val relativeGameTime = level.gameTime + partialTick
        val rotation = relativeGameTime * 2

        val showItem =
            if (contract.allMatchingItems.isEmpty()) ItemStack.EMPTY
            else contract.allMatchingItems[Mth.floor(relativeGameTime / 30.0f) % contract.allMatchingItems.size]

        poseStack.pushPose()
        poseStack.translate(translate.x, translate.y, translate.z)
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