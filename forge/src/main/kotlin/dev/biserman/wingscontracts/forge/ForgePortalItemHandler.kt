package dev.biserman.wingscontracts.forge

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import dev.biserman.wingscontracts.container.ISidedPortalItemHandler
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.items.IItemHandler
import kotlin.math.min

class ForgePortalItemHandler(val portal: ContractPortalBlockEntity) :
    IItemHandler,
    ISidedPortalItemHandler,
    ICapabilityProvider {
    override fun getSlots() = portal.containerSize

    override fun getStackInSlot(i: Int): ItemStack = portal.getItem(i)

    override fun insertItem(
        i: Int,
        itemStack: ItemStack,
        simulate: Boolean
    ): ItemStack {
        if (!portal.canPlaceItem(i, itemStack)) {
            return itemStack
        }

        return portal.tryMoveInInputItem(itemStack, i, simulate)
    }

    override fun extractItem(i: Int, count: Int, simulate: Boolean): ItemStack {
        if (!portal.canTakeItem(i)) {
            return ItemStack.EMPTY
        }

        return portal.removeItem(i, count, simulate)
    }

    override fun getSlotLimit(i: Int) = min(64, getStackInSlot(i).maxStackSize)

    override fun isItemValid(i: Int, itemStack: ItemStack) = portal.canPlaceItem(i, itemStack)

    override fun <T> getCapability(
        capability: Capability<T>,
        direction: Direction?
    ): LazyOptional<T?> = LazyOptional.of { this }.cast()

    companion object {
        fun attachCapabilities(event: AttachCapabilitiesEvent<BlockEntity>) {
            WingsContractsMod.LOGGER.info("attaching portal capability")
            val entity = event.`object`
            when (entity) {
                is ContractPortalBlockEntity -> event.addCapability(
                    ContractPortalBlockEntity.STORAGE_ID,
                    entity.itemHandler as ForgePortalItemHandler
                )
            }
        }
    }
}