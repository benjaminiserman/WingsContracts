package dev.biserman.wingscontracts.block

import dev.biserman.wingscontracts.block.state.properties.ContractPortalMode
import dev.biserman.wingscontracts.registry.BlockEntityRegistry
import net.minecraft.core.BlockPos
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity
import net.minecraft.world.level.block.entity.Hopper
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import java.util.stream.Collectors
import kotlin.math.min

class ContractPortalBlockEntity(blockPos: BlockPos, blockState: BlockState) :
    BaseContainerBlockEntity(BlockEntityRegistry.CONTRACT_PORTAL.get(), blockPos, blockState),
    Hopper {
    private var cooldownTime: Int
    private var items: NonNullList<ItemStack>

    init {
        this.items = NonNullList.withSize(27, ItemStack.EMPTY)
        this.cooldownTime = -1
    }

    override fun getDefaultName(): Component = Component.translatable("container.wingscontracts.contractportal")

    override fun load(compoundTag: CompoundTag) {
        super.load(compoundTag)
        this.items = NonNullList.withSize(
            containerSize, ItemStack.EMPTY
        )
        ContainerHelper.loadAllItems(compoundTag, items)
        this.cooldownTime = compoundTag.getInt("SuckCooldown")
    }

    override fun saveAdditional(compoundTag: CompoundTag) {
        super.saveAdditional(compoundTag)
        ContainerHelper.saveAllItems(compoundTag, items)
        compoundTag.putInt("SuckCooldown", this.cooldownTime)
    }

    override fun getContainerSize(): Int = items.size

    private fun setCooldown(i: Int) {
        this.cooldownTime = i
    }

    override fun getLevelX(): Double = worldPosition.x.toDouble() + 0.5

    override fun getLevelY(): Double = worldPosition.y.toDouble() + 0.5

    override fun getLevelZ(): Double = worldPosition.z.toDouble() + 0.5

    private val isOnCooldown: Boolean
        get() = this.cooldownTime > 0

    private fun inventoryFull(): Boolean {
        val iterator: Iterator<ItemStack> = items.iterator()

        var itemStack: ItemStack
        do {
            if (!iterator.hasNext()) {
                return true
            }

            itemStack = iterator.next()
        } while (!itemStack.isEmpty && itemStack.count == itemStack.maxStackSize)

        return false
    }

    override fun removeItem(i: Int, j: Int): ItemStack = ContainerHelper.removeItem(this.items, i, j)

    override fun setItem(i: Int, itemStack: ItemStack) {
        items[i] = itemStack
        if (itemStack.count > this.maxStackSize) {
            itemStack.count = this.maxStackSize
        }
        this.setChanged()
    }

    override fun isEmpty(): Boolean = items.stream().allMatch { obj: ItemStack -> obj.isEmpty }

    override fun getItem(i: Int): ItemStack = items[i]

    override fun stillValid(player: Player): Boolean = Container.stillValidBlockEntity(this, player)

    var contractSlot: ItemStack
        get() = getItem(0)
        set(itemStack) {
            setItem(0, itemStack)
        }

    override fun clearContent() {
        items.clear()
    }

    override fun removeItemNoUpdate(i: Int): ItemStack = ContainerHelper.takeItem(this.items, i)

    override fun createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu? {
        return if (this.canOpen(player)) {
            createMenu(i, inventory)
        } else {
            null
        }
    }

    override fun createMenu(i: Int, inventory: Inventory): AbstractContainerMenu = ChestMenu.oneRow(i, inventory)

    override fun canOpen(player: Player): Boolean = false

    override fun getUpdateTag(): CompoundTag = this.saveWithoutMetadata()

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? = ClientboundBlockEntityDataPacket.create(this)

    override fun canTakeItem(container: Container, i: Int, itemStack: ItemStack): Boolean = false

    companion object {
        fun serverTick(
            level: Level, blockPos: BlockPos, blockState: BlockState,
            contractPortalBlockEntity: ContractPortalBlockEntity
        ): Boolean {
            --contractPortalBlockEntity.cooldownTime

            if (contractPortalBlockEntity.blockState.getValue(ContractPortalBlock.MODE) != ContractPortalMode.LIT) {
                return false
            }

            if (contractPortalBlockEntity.isOnCooldown) {
                return false
            }
            contractPortalBlockEntity.setCooldown(0)

            if (contractPortalBlockEntity.inventoryFull()) {
                return false
            }

            if (!suckInItems(level, contractPortalBlockEntity)) {
                return false
            }

            contractPortalBlockEntity.setCooldown(10)
            setChanged(level, blockPos, blockState)
            return true
        }

        private fun getItemsAtAndAbove(level: Level, portal: ContractPortalBlockEntity): List<ItemEntity> {
            return portal.suckShape.toAabbs().stream().flatMap { aABB: AABB ->
                level.getEntitiesOfClass(
                    ItemEntity::class.java,
                    aABB.move(portal.levelX - 0.5, portal.levelY - 0.5, portal.levelZ - 0.5),
                    EntitySelector.ENTITY_STILL_ALIVE
                ).stream()
            }.collect(Collectors.toList())
        }

        private fun suckInItems(level: Level, portal: ContractPortalBlockEntity): Boolean {
            val aboveItems = getItemsAtAndAbove(level, portal).iterator()

            var itemEntity: ItemEntity
            do {
                if (!aboveItems.hasNext()) {
                    return false
                }

                itemEntity = aboveItems.next()
            } while (!addItem(portal, itemEntity))

            return true
        }

        private fun addItem(portal: ContractPortalBlockEntity, itemEntity: ItemEntity): Boolean {
            var didConsumeAll = false
            val itemStack = itemEntity.item.copy()
            val itemStack2 = addItem(portal, itemStack)
            if (itemStack2.isEmpty) {
                didConsumeAll = true
                itemEntity.discard()
            } else {
                itemEntity.item = itemStack2
            }

            return didConsumeAll
        }

        private fun addItem(portal: ContractPortalBlockEntity, itemStack: ItemStack): ItemStack {
            var mutItemStack = itemStack
            val size = portal.containerSize

            var i = 1
            while (i < size && !mutItemStack.isEmpty) {
                mutItemStack = tryMoveInItem(portal, mutItemStack, i)
                ++i
            }

            return mutItemStack
        }

        private fun tryMoveInItem(portal: ContractPortalBlockEntity, itemStack: ItemStack, i: Int): ItemStack {
            var mutItemStack = itemStack
            val itemStack2 = portal.getItem(i)
            if (portal.canPlaceItem(i, mutItemStack)) {
                var movedItems = false
                if (itemStack2.isEmpty) {
                    portal.setItem(i, mutItemStack)
                    mutItemStack = ItemStack.EMPTY
                    movedItems = true
                } else if (canMergeItems(itemStack2, mutItemStack)) {
                    val j = mutItemStack.maxStackSize - itemStack2.count
                    val k = min(mutItemStack.count.toDouble(), j.toDouble()).toInt()
                    mutItemStack.shrink(k)
                    itemStack2.grow(k)
                    movedItems = k > 0
                }

                if (movedItems) {
                    portal.setCooldown(10)
                }

                portal.setChanged()
            }

            return mutItemStack
        }

        private fun canMergeItems(itemStack: ItemStack, itemStack2: ItemStack): Boolean {
            return itemStack.count <= itemStack.maxStackSize
                    && ItemStack.isSameItemSameTags(itemStack, itemStack2)
        }
    }
}
