package dev.biserman.wingscontracts.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntityType

@Suppress("MemberVisibilityCanBePrivate")
object BlockEntityRegistry {
    val BLOCK_ENTITIES: DeferredRegister<BlockEntityType<*>> = DeferredRegister
        .create(WingsContractsMod.MOD_ID, Registries.BLOCK_ENTITY_TYPE)

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    val CONTRACT_PORTAL: RegistrySupplier<BlockEntityType<ContractPortalBlockEntity>> = BLOCK_ENTITIES
        .register(
            "contract_portal_be"
        ) {
            BlockEntityType.Builder
                .of({ blockPos, blockState ->
                    ContractPortalBlockEntity(
                        blockPos,
                        blockState
                    )
                }, BlockRegistry.CONTRACT_PORTAL.get()).build(null)
        }

    @JvmStatic
    fun register() {
        BLOCK_ENTITIES.register()
    }
}
