package dev.biserman.wingscontracts.neoforge.block

import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import net.minecraft.core.Direction
import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.neoforge.capabilities.ICapabilityProvider

class PortalItemHandlerCapabilityProvider(val portal: ContractPortalBlockEntity) : ICapabilityProvider {
    override fun <T : Any?> getCapability(
        capability: Capability<T>,
        arg: Direction?
    ): LazyOptional<T?> = getCapability(capability)

    override fun <T : Any?> getCapability(capability: Capability<T>): LazyOptional<T?> {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return LazyOptional
                .of { ForgePortalItemHandler(portal) }
                .cast()
        }

        return LazyOptional.empty()
    }

    companion object {
        fun attachCapabilities(event: AttachCapabilitiesEvent<BlockEntity>) {
            val entity = event.`object`
            if (entity is ContractPortalBlockEntity) {
                event.addCapability(
                    ContractPortalBlockEntity.Companion.STORAGE_ID,
                    PortalItemHandlerCapabilityProvider(entity)
                )
            }
        }
    }
}