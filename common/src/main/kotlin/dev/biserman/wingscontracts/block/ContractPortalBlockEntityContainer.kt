@file:OptIn(ExperimentalStdlibApi::class)

package dev.biserman.wingscontracts.block

import net.minecraft.core.Direction
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

// this is for hopper compat
class ContractPortalBlockEntityContainer(val portal: ContractPortalBlockEntity) : WorldlyContainer {
    override fun getSlotsForFace(direction: Direction): IntArray? {
        return when (direction) {
            Direction.DOWN -> intArrayOf(0)
            else -> (1..ContractPortalBlockEntity.CONTAINER_SIZE).toList().toIntArray()
        }
    }

    override fun canPlaceItemThroughFace(
        i: Int,
        itemStack: ItemStack,
        direction: Direction?
    ): Boolean = canPlaceItem(i, itemStack)

    override fun canTakeItemThroughFace(
        i: Int,
        itemStack: ItemStack,
        direction: Direction
    ): Boolean = canTakeItem(i, itemStack)

    override fun canPlaceItem(i: Int, itemStack: ItemStack): Boolean = i != 0

    override fun canTakeItem(container: Container, i: Int, itemStack: ItemStack) = canTakeItem(i, itemStack)
    fun canTakeItem(i: Int, itemStack: ItemStack) = i == 0

    override fun getContainerSize(): Int = ContractPortalBlockEntity.CONTAINER_SIZE

    override fun isEmpty(): Boolean = portal.cachedInput.all { it.isEmpty } && portal.cachedRewards.isEmpty

    override fun getItem(i: Int): ItemStack? {
        return if (i == 0) portal.cachedRewards else portal.cachedInput[i - 1]
    }

    override fun removeItem(i: Int, j: Int): ItemStack? {
        if (i < 0 || i >= containerSize || j <= 0) {
            return ItemStack.EMPTY
        }

        return if (i == 0) {
            portal.cachedRewards.split(j)
        } else {
            portal.cachedInput[i - 1].split(j)
        }
    }

    override fun removeItemNoUpdate(i: Int): ItemStack? {
        if (i < 0 || i >= containerSize) {
            return ItemStack.EMPTY
        }

        if (i == 0) {
            val output = portal.cachedRewards
            portal.cachedRewards = ItemStack.EMPTY
            return output
        } else {
            return ContainerHelper.takeItem(portal.cachedInput, i - 1)
        }
    }

    override fun setItem(i: Int, itemStack: ItemStack) {
        if (i == 0) {
            portal.cachedRewards = itemStack
        } else {
            portal.cachedInput[i - 1] = itemStack
        }
    }

    override fun setChanged() {
        portal.setChanged()
    }

    override fun stillValid(player: Player): Boolean = Container.stillValidBlockEntity(portal, player)

    override fun clearContent() {
        portal.cachedRewards = ItemStack.EMPTY
        for (i in 0..<portal.cachedInput.size) {
            portal.cachedInput[i] = ItemStack.EMPTY
        }
    }
}