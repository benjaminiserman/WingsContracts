package dev.biserman.wingscontracts.block

import dev.biserman.wingscontracts.block.state.properties.ContractPortalMode
import dev.biserman.wingscontracts.item.ContractItem
import dev.biserman.wingscontracts.registry.BlockEntityRegistry
import net.minecraft.core.BlockPos
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import java.util.stream.Collectors
import kotlin.math.min

class ContractPortalBlockEntity(blockPos: BlockPos, blockState: BlockState) :
    BlockEntity(BlockEntityRegistry.CONTRACT_PORTAL.get(), blockPos, blockState) {
    var cooldownTime: Int
    var contractSlot: ItemStack
    var cachedRewards: ItemStack
    var cachedInput: NonNullList<ItemStack>

    fun getLevelX(): Double = worldPosition.x.toDouble() + 0.5
    fun getLevelY(): Double = worldPosition.y.toDouble() + 0.5
    fun getLevelZ(): Double = worldPosition.z.toDouble() + 0.5

    init {
        this.cooldownTime = -1
        this.contractSlot = ItemStack.EMPTY
        this.cachedRewards = ItemStack.EMPTY
        this.cachedInput = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY)
    }

    override fun load(compoundTag: CompoundTag) {
        super.load(compoundTag)

        this.cooldownTime = compoundTag.getInt("SuckCooldown")
        this.contractSlot = ItemStack.of(compoundTag.getCompound("ContractSlot"))
        this.cachedRewards = ItemStack.of(compoundTag.getCompound("CachedRewards"))
        ContainerHelper.loadAllItems(compoundTag, cachedInput)
    }

    override fun saveAdditional(compoundTag: CompoundTag) {
        super.saveAdditional(compoundTag)
        compoundTag.putInt("SuckCooldown", this.cooldownTime)

        val contractSlotTag = CompoundTag()
        contractSlot.save(contractSlotTag)
        compoundTag.put("ContractSlot", contractSlotTag)

        val cachedRewardsTag = CompoundTag()
        contractSlot.save(cachedRewardsTag)
        compoundTag.put("CachedRewards", cachedRewardsTag)

        ContainerHelper.saveAllItems(compoundTag, cachedInput)
    }

    private fun setCooldown(i: Int) {
        this.cooldownTime = i
    }

    override fun getUpdateTag(): CompoundTag = this.saveWithoutMetadata()

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? = ClientboundBlockEntityDataPacket.create(this)

    private fun inventoryFull(): Boolean {
        val iterator: Iterator<ItemStack> = cachedInput.iterator()

        var itemStack: ItemStack
        do {
            if (!iterator.hasNext()) {
                return true
            }

            itemStack = iterator.next()
        } while (!itemStack.isEmpty && itemStack.count == itemStack.maxStackSize)

        return false
    }

    fun getItem(i: Int): ItemStack = cachedInput[i]

    fun setItem(i: Int, itemStack: ItemStack) {
        cachedInput[i] = itemStack
        if (itemStack.count > MAX_STACK_SIZE) {
            itemStack.count = MAX_STACK_SIZE
        }
        this.setChanged()
    }

    companion object {
        fun serverTick(
            level: Level, blockPos: BlockPos, blockState: BlockState,
            portal: ContractPortalBlockEntity
        ): Boolean {
            --portal.cooldownTime
            if (portal.cooldownTime > 0) {
                return false
            }
            portal.setCooldown(0)

            when (portal.blockState.getValue(ContractPortalBlock.MODE)) {
                ContractPortalMode.UNLIT -> {}
                ContractPortalMode.LIT -> {
                    if (portal.contractSlot.isEmpty) {
                        return false
                    }

                    val didConsume = tryConsume(portal)
                    val didSuck = suckInItems(level, portal)

                    portal.setCooldown(10)

                    if (didConsume || didSuck) {
                        setChanged(level, blockPos, blockState)
                        return true
                    }
                }

                ContractPortalMode.COIN -> {}
                else -> {}
            }

            return false
        }

        private fun getItemsAtAndAbove(level: Level, portal: ContractPortalBlockEntity): List<ItemEntity> {
            return Companion.getSuckShape().toAabbs().stream().flatMap { aABB: AABB ->
                level.getEntitiesOfClass(
                    ItemEntity::class.java,
                    aABB.move(portal.getLevelX() - 0.5, portal.getLevelY() - 0.5, portal.getLevelZ() - 0.5),
                    EntitySelector.ENTITY_STILL_ALIVE
                ).stream()
            }.collect(Collectors.toList())
        }

        private fun tryConsume(portal: ContractPortalBlockEntity): Boolean {
            for (itemStack in portal.cachedInput) {
                if (itemStack.isEmpty) {
                    continue
                }

                if (ContractItem.matches(portal.contractSlot, itemStack)) {
                    return ContractItem.consume(portal.contractSlot, itemStack) > 0
                }
            }

            return false
        }

        private fun suckInItems(level: Level, portal: ContractPortalBlockEntity): Boolean {
            val aboveItems = getItemsAtAndAbove(level, portal).iterator()

            if (portal.inventoryFull()) {
                return false
            }

            while (aboveItems.hasNext()) {
                val itemEntity = aboveItems.next()

                if (!ContractItem.matches(portal.contractSlot, itemEntity.item)) {
                    continue
                }

                if (addItem(portal, itemEntity)) {
                    return true
                }
            }

            return false
        }

        private fun addItem(portal: ContractPortalBlockEntity, itemEntity: ItemEntity): Boolean {
            var didConsumeAll = false
            val beforeStack = itemEntity.item.copy()
            val afterStack = addItem(portal, beforeStack)
            if (afterStack.isEmpty) {
                didConsumeAll = true
                itemEntity.discard()
            } else {
                itemEntity.item = afterStack
            }

            return didConsumeAll
        }

        private fun addItem(portal: ContractPortalBlockEntity, itemStack: ItemStack): ItemStack {
            var mutItemStack = itemStack
            val size = CONTAINER_SIZE

            var i = 0
            while (i < size && !mutItemStack.isEmpty) {
                mutItemStack = tryMoveInItem(portal, mutItemStack, i)
                ++i
            }

            return mutItemStack
        }

        private fun tryMoveInItem(portal: ContractPortalBlockEntity, itemStack: ItemStack, i: Int): ItemStack {
            var mutItemStack = itemStack
            val stackAtSlot = portal.getItem(i)
            var movedItems = false
            if (stackAtSlot.isEmpty) {
                portal.setItem(i, mutItemStack)
                mutItemStack = ItemStack.EMPTY
                movedItems = true
            } else if (canMergeItems(stackAtSlot, mutItemStack)) {
                val j = mutItemStack.maxStackSize - stackAtSlot.count
                val k = min(mutItemStack.count.toDouble(), j.toDouble()).toInt()
                mutItemStack.shrink(k)
                stackAtSlot.grow(k)
                movedItems = k > 0
            }

            if (movedItems) {
                portal.setCooldown(10)
            }

            portal.setChanged()

            return mutItemStack
        }

        private fun canMergeItems(itemStack: ItemStack, itemStack2: ItemStack): Boolean {
            return itemStack.count <= itemStack.maxStackSize
                    && ItemStack.isSameItemSameTags(itemStack, itemStack2)
        }

        private val INSIDE: VoxelShape = Block.box(2.0, 11.0, 2.0, 14.0, 16.0, 14.0)
        private val ABOVE: VoxelShape = Block.box(0.0, 16.0, 0.0, 16.0, 32.0, 16.0)
        private val SUCK: VoxelShape = Shapes.or(INSIDE, ABOVE)
        private const val CONTAINER_SIZE = 27
        private const val MAX_STACK_SIZE = 64
        private fun getSuckShape(): VoxelShape = SUCK
    }
}
