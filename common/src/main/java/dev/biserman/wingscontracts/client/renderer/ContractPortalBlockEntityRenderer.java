package dev.biserman.wingscontracts.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import dev.biserman.wingscontracts.block.ContractPortalBlockEntity;
import dev.biserman.wingscontracts.item.ContractItem;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.LightLayer;

public class ContractPortalBlockEntityRenderer implements BlockEntityRenderer<ContractPortalBlockEntity> {
    private final BlockEntityRendererProvider.Context context;

    public ContractPortalBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(ContractPortalBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        var itemStack = blockEntity.getItem(0);


        if (!(itemStack.getItem() instanceof ContractItem)) {
            poseStack.popPose();
            return;
        }

        var showItem = ContractItem.targetItem(itemStack);
        if (showItem == null) {
            poseStack.popPose();
            return;
        }

        var level = blockEntity.getLevel();
        if (level == null) {
            poseStack.popPose();
            return;
        }

        var blockPos = blockEntity.getBlockPos().above();
        var relativeGameTime = level.getGameTime() + partialTick;
        var rotation = relativeGameTime * 2;

        poseStack.translate(0.5, 1.3, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) rotation));
        this.context.getItemRenderer().renderStatic(showItem, ItemDisplayContext.GROUND,
                LightTexture.pack(level.getBrightness(LightLayer.BLOCK, blockPos),
                        level.getBrightness(LightLayer.SKY, blockPos)),
                OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, level, 0);

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
        poseStack.popPose();
    }
}
