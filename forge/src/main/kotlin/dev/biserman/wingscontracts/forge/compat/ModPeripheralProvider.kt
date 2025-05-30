package dev.biserman.wingscontracts.forge.compat

import dan200.computercraft.api.ForgeComputerCraftAPI
import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.peripheral.IPeripheralProvider
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import dev.biserman.wingscontracts.compat.computercraft.ContractPortalPeripheral
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.util.LazyOptional

class ModPeripheralProvider : IPeripheralProvider {
    val blockEntityPeripheralMap = mutableMapOf<BlockEntity, IPeripheral>()

    override fun getPeripheral(level: Level, blockPos: BlockPos, direction: Direction): LazyOptional<IPeripheral> {
        return when (val blockEntity = level.getBlockEntity(blockPos)) {
            is ContractPortalBlockEntity -> {
                val peripheral = blockEntityPeripheralMap.getOrPut(blockEntity) { ContractPortalPeripheral(blockEntity) }
                return LazyOptional.of { peripheral }
            }

            else -> LazyOptional.of(null)
        }
    }

    companion object {
        fun register() {
            ForgeComputerCraftAPI.registerPeripheralProvider(ModPeripheralProvider())
        }
    }
}