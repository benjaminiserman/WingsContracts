package dev.biserman.wingscontracts.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.entity.FakeItemEntity
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory

@Suppress("MemberVisibilityCanBePrivate")
object ModEntityRegistry {
    val ENTITIES: DeferredRegister<EntityType<*>> = DeferredRegister
        .create(WingsContractsMod.MOD_ID, Registries.ENTITY_TYPE)

    val FAKE_ITEM: RegistrySupplier<EntityType<FakeItemEntity>> = ENTITIES
        .register(
            "fake_item"
        ) {
            EntityType.Builder.of<FakeItemEntity>(::FakeItemEntity, MobCategory.MISC).sized(0.5f, 0.5f)
                .build(WingsContractsMod.prefix("fake_item").toString())
        }

    @JvmStatic
    fun register() {
        ENTITIES.register()
    }
}
