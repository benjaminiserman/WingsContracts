package dev.biserman.wingscontracts.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dev.biserman.wingscontracts.WingsContractsMod
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent

object ModSoundRegistry {
    val SOUNDS: DeferredRegister<SoundEvent> = DeferredRegister.create(
        WingsContractsMod.MOD_ID,
        Registries.SOUND_EVENT
    )

    val PORTAL_SPIT: RegistrySupplier<SoundEvent> = SOUNDS.register(
        "portal_spit"
    ) { SoundEvent.createVariableRangeEvent(ResourceLocation(WingsContractsMod.MOD_ID, "portal_spit")) }

    val PORTAL_ACCEPT: RegistrySupplier<SoundEvent> = SOUNDS.register(
        "portal_accept"
    ) { SoundEvent.createVariableRangeEvent(ResourceLocation(WingsContractsMod.MOD_ID, "portal_accept")) }

    val PORTAL_REJECT: RegistrySupplier<SoundEvent> = SOUNDS.register(
        "portal_reject"
    ) { SoundEvent.createVariableRangeEvent(ResourceLocation(WingsContractsMod.MOD_ID, "portal_reject")) }

    val PORTAL_ADD_CONTRACT: RegistrySupplier<SoundEvent> = SOUNDS.register(
        "portal_add_contract"
    ) { SoundEvent.createVariableRangeEvent(ResourceLocation(WingsContractsMod.MOD_ID, "portal_add_contract")) }

    val PORTAL_REMOVE_CONTRACT: RegistrySupplier<SoundEvent> = SOUNDS.register(
        "portal_remove_contract"
    ) { SoundEvent.createVariableRangeEvent(ResourceLocation(WingsContractsMod.MOD_ID, "portal_remove_contract")) }

    @JvmStatic
    fun register() {
        SOUNDS.register()
    }
}