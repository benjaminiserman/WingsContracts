package dev.biserman.wingscontracts.forge.block

import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import dev.biserman.wingscontracts.container.ISidedPortalItemHandler
import net.minecraft.world.item.ItemStack
import net.minecraftforge.common.capabilities.AutoRegisterCapability
import net.minecraftforge.items.IItemHandler
import kotlin.math.min

@AutoRegisterCapability
class ForgePortalItemHandler(val portal: ContractPortalBlockEntity) :
    IItemHandler,
    ISidedPortalItemHandler {
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
}