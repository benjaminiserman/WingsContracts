package dev.biserman.wingscontracts.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.biserman.wingscontracts.core.Contract;
import dev.biserman.wingscontracts.item.ContractItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow
    public abstract void renderStatic(ItemStack itemStack, ItemDisplayContext itemDisplayContext, int i, int j, PoseStack poseStack, MultiBufferSource multiBufferSource, @Nullable Level level, int k);

    @Inject(method = "render", at = @At("RETURN"))
    public void render(
            ItemStack itemStack,
            ItemDisplayContext itemDisplayContext,
            boolean bl,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int i,
            int j,
            BakedModel bakedModel,
            CallbackInfo ci) {
        if (!itemStack.isEmpty()
                && itemStack.getItem() instanceof ContractItem
                && itemDisplayContext == ItemDisplayContext.GUI) {
            var displayItem = Contract.Companion.getDisplayItem(itemStack, 0f); // make this use deltaTime somehow
            poseStack.pushPose();
            poseStack.scale(0.5f, 0.5f, 0.5f);
            poseStack.translate(0, 0, +1.0);
            renderStatic(displayItem, itemDisplayContext, i, j, poseStack, multiBufferSource, Minecraft.getInstance().level, 1);
            poseStack.popPose();
        }
    }
}