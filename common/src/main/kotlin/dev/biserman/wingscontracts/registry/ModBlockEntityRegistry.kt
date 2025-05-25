package dev.biserman.wingscontracts.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import dev.biserman.wingscontracts.block.ContractSpigotBlockEntity
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntityType

@Suppress("MemberVisibilityCanBePrivate")
object ModBlockEntityRegistry {
    val BLOCK_ENTITIES: DeferredRegister<BlockEntityType<*>> = DeferredRegister
        .create(WingsContractsMod.MOD_ID, Registries.BLOCK_ENTITY_TYPE)

    val CONTRACT_PORTAL: RegistrySupplier<BlockEntityType<ContractPortalBlockEntity>> = BLOCK_ENTITIES
        .register(
            "contract_portal_be"
        ) {
            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            BlockEntityType.Builder
                .of({ blockPos, blockState ->
                    ContractPortalBlockEntity(
                        blockPos,
                        blockState
                    )
                }, ModBlockRegistry.CONTRACT_PORTAL.get()).build(null)
        }

    val CONTRACT_SPIGOT: RegistrySupplier<BlockEntityType<ContractSpigotBlockEntity>> = BLOCK_ENTITIES
        .register(
            "contract_spigot_be"
        ) {
            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            BlockEntityType.Builder
                .of({ blockPos, blockState ->
                    ContractSpigotBlockEntity(
                        blockPos,
                        blockState
                    )
                }, ModBlockRegistry.CONTRACT_SPIGOT.get()).build(null)
        }


    @JvmStatic
    fun register() {
        BLOCK_ENTITIES.register()
    }
}
