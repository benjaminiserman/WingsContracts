package dev.biserman.wingscontracts.forge.mixin;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity;
import dev.biserman.wingscontracts.data.LoadedContracts;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Pseudo;

import java.util.List;

@SuppressWarnings({"DataFlowIssue"})
@Mixin(ContractPortalBlockEntity.class)
@Pseudo
public class PortalGoggleInformationMixin implements IHaveGoggleInformation {
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        var portal = (ContractPortalBlockEntity) (Object) this;
        var contract = LoadedContracts.INSTANCE.get(portal.getContractSlot());
        if (contract == null) {
            return false;
        }

        return contract.addToGoggleTooltip(portal, tooltip, isPlayerSneaking);
    }
}