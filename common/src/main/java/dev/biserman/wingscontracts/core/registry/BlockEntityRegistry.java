package dev.biserman.wingscontracts.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.biserman.wingscontracts.WingsContractsMod;
import dev.biserman.wingscontracts.core.block.ContractPortalBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
            .create(WingsContractsMod.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<ContractPortalBlockEntity>> CONTRACT_PORTAL = BLOCK_ENTITIES
            .register("contract_portal_be", () -> BlockEntityType.Builder
                    .of(ContractPortalBlockEntity::new, BlockRegistry.CONTRACT_PORTAL.get()).build(null));

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}
