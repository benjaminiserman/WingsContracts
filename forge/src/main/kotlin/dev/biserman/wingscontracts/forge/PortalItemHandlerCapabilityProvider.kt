package dev.biserman.wingscontracts.forge

import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import net.minecraft.core.Direction
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.event.AttachCapabilitiesEvent

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
                    ContractPortalBlockEntity.STORAGE_ID,
                    PortalItemHandlerCapabilityProvider(entity)
                )
            }
        }
    }
}

