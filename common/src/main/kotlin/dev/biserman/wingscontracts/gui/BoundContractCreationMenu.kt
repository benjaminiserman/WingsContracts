package dev.biserman.wingscontracts.gui

import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import dev.biserman.wingscontracts.registry.ModMenuRegistry
import dev.biserman.wingscontracts.server.CreateBoundContractsMessage
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.inventory.Slot
import net.minecraft.world.inventory.TransientCraftingContainer
import net.minecraft.world.item.ItemStack

class BoundContractCreationMenu(id: Int, inventory: Inventory) :
    AbstractContainerMenu(ModMenuRegistry.BOUND_CONTRACT_CREATION.get(), id) {

    val leftSlots: CraftingContainer = TransientCraftingContainer(this, 3, 3);
    val rightSlots: CraftingContainer = TransientCraftingContainer(this, 3, 3);

    init {
        for (i in 0..2) {
            for (j in 0..2) {
                this.addSlot(MatchingItemSlot(leftSlots, j + i * 3, 26 + j * 18, 18 + i * 18))
            }
        }

        for (i in 0..2) {
            for (j in 0..2) {
                this.addSlot(MatchingItemSlot(rightSlots, j + i * 3, 98 + j * 18, 18 + i * 18))
            }
        }

        for (i in 0..<3) {
            for (j in 0..<9) {
                this.addSlot(Slot(inventory, j + i * 9 + 9, 8 + j * 18, i * 18 + 107))
            }
        }

        for (i in 0..<9) {
            this.addSlot(Slot(inventory, i, 8 + i * 18, 165))
        }
    }

    override fun quickMoveStack(
        player: Player,
        i: Int
    ): ItemStack {
        if (i < 18) {
            slots[i].setByPlayer(ItemStack.EMPTY)
        }

        return ItemStack.EMPTY
    }

    override fun stillValid(player: Player): Boolean = true

    fun isValidContract(leftCount: Int, rightCount: Int): Boolean {
        if (leftSlots.isEmpty || rightSlots.isEmpty) {
            return false
        }

        if (!leftSlots.items.any { !it.isEmpty && it.maxStackSize * ContractPortalBlockEntity.containerSize >= leftCount }) {
            return false
        }

        if (!rightSlots.items.any { !it.isEmpty && it.maxStackSize * ContractPortalBlockEntity.containerSize >= rightCount }) {
            return false
        }

        return true
    }

    fun submit(leftCount: Int, rightCount: Int, name: String) {
        val submitTag = CompoundTag()

        val left = CompoundTag()
        ContainerHelper.saveAllItems(left, NonNullList(leftSlots.items, ItemStack.EMPTY))
        left.putInt("Count", leftCount)
        submitTag.put("Left", left)

        val right = CompoundTag()
        ContainerHelper.saveAllItems(right, NonNullList(rightSlots.items, ItemStack.EMPTY))
        right.putInt("Count", rightCount)
        submitTag.put("Right", right)

        submitTag.putString("Name", name)

        CreateBoundContractsMessage(submitTag).sendToServer()
    }

    class MatchingItemSlot(container: Container, i: Int, x: Int, y: Int) : Slot(container, i, x, y) {
        override fun remove(i: Int): ItemStack {
            super.remove(i)
            return ItemStack.EMPTY
        }

        override fun safeTake(i: Int, j: Int, player: Player): ItemStack {
            setByPlayer(ItemStack.EMPTY)
            return ItemStack.EMPTY
        }

        override fun safeInsert(itemStack: ItemStack, i: Int): ItemStack {
            setByPlayer(itemStack.copyWithCount(1))
            return itemStack
        }
    }
}