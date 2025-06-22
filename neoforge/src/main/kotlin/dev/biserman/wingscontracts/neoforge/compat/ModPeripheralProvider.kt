package dev.biserman.wingscontracts.neoforge.compat

import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.peripheral.PeripheralCapability
import dev.architectury.platform.Platform
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import dev.biserman.wingscontracts.compat.CompatMods
import dev.biserman.wingscontracts.compat.computercraft.ContractPortalPeripheral
import dev.biserman.wingscontracts.registry.ModBlockRegistry
import net.minecraft.core.Direction
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.capabilities.ICapabilityProvider
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent

object ModPeripheralProvider : ICapabilityProvider<ContractPortalBlockEntity, Direction, IPeripheral> {
    override fun getCapability(
        portal: ContractPortalBlockEntity,
        direction: Direction
    ): IPeripheral = ContractPortalPeripheral(portal)

    @SubscribeEvent
    fun registerCapability(event: RegisterCapabilitiesEvent) {
        if (!Platform.isModLoaded(CompatMods.COMPUTERCRAFT)) {
            return
        }

        event.registerBlock(
            PeripheralCapability.get(),
            { level, pos, _, blockEntity, side ->
                getCapability(
                    blockEntity as ContractPortalBlockEntity,
                    side ?: Direction.UP
                )
            },
            ModBlockRegistry.CONTRACT_PORTAL.get()
        )
    }
}