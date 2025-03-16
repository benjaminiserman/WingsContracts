package dev.biserman.wingscontracts.mixin;

import dev.biserman.wingscontracts.client.ClientContractTooltip;
import dev.biserman.wingscontracts.item.ContractTooltip;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentMixin {
    @Inject(method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;", at = @At(value = "HEAD"), cancellable = true)
    private static void create(TooltipComponent tooltipComponent, CallbackInfoReturnable<ClientTooltipComponent> cir) {
        if (tooltipComponent instanceof ContractTooltip contractTooltip) {
            cir.setReturnValue(new ClientContractTooltip(contractTooltip));
        }
    }
}
