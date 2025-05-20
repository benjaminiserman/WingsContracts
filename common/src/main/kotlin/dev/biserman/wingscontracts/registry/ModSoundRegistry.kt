package dev.biserman.wingscontracts.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.WingsContractsMod.prefix
import net.minecraft.core.registries.Registries
import net.minecraft.sounds.SoundEvent

object ModSoundRegistry {
    val SOUNDS: DeferredRegister<SoundEvent> = DeferredRegister.create(
        WingsContractsMod.MOD_ID,
        Registries.SOUND_EVENT
    )

    val PORTAL_SPIT: RegistrySupplier<SoundEvent> = SOUNDS.register(
        "portal_spit"
    ) { SoundEvent.createVariableRangeEvent(prefix("portal_spit")) }

    val PORTAL_ACCEPT: RegistrySupplier<SoundEvent> = SOUNDS.register(
        "portal_accept"
    ) { SoundEvent.createVariableRangeEvent(prefix("portal_accept")) }

    val PORTAL_REJECT: RegistrySupplier<SoundEvent> = SOUNDS.register(
        "portal_reject"
    ) { SoundEvent.createVariableRangeEvent(prefix("portal_reject")) }

    val PORTAL_ADD_CONTRACT: RegistrySupplier<SoundEvent> = SOUNDS.register(
        "portal_add_contract"
    ) { SoundEvent.createVariableRangeEvent(prefix("portal_add_contract")) }

    val PORTAL_REMOVE_CONTRACT: RegistrySupplier<SoundEvent> = SOUNDS.register(
        "portal_remove_contract"
    ) { SoundEvent.createVariableRangeEvent(prefix("portal_remove_contract")) }

    val WRITE_CONTRACT: RegistrySupplier<SoundEvent> = SOUNDS.register(
        "write_contract"
    ) { SoundEvent.createVariableRangeEvent(prefix("write_contract")) }
    val COMPLETE_CONTRACT: RegistrySupplier<SoundEvent> = SOUNDS.register(
        "complete_contract"
    ) { SoundEvent.createVariableRangeEvent(prefix("complete_contract")) }

    @JvmStatic
    fun register() {
        SOUNDS.register()
    }
}