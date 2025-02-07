package dev.biserman.wingscontracts.block

import dev.biserman.wingscontracts.block.ContractPortalBlock.Companion.MODE
import dev.biserman.wingscontracts.block.state.properties.ContractPortalMode
import dev.biserman.wingscontracts.item.ContractItem
import dev.biserman.wingscontracts.registry.BlockEntityRegistry
import net.minecraft.core.BlockPos
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock.UPDATE_ALL
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import java.util.stream.Collectors
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class ContractPortalBlockEntity(blockPos: BlockPos, blockState: BlockState) :
    BlockEntity(BlockEntityRegistry.CONTRACT_PORTAL.get(), blockPos, blockState), Container {
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
        cachedRewards.save(cachedRewardsTag)
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

    override fun clearContent() {
        cachedRewards = ItemStack.EMPTY
    }

    override fun getContainerSize(): Int = 1
    override fun isEmpty() = cachedRewards.count == 0
    override fun getItem(i: Int) = cachedRewards

    override fun setItem(i: Int, itemStack: ItemStack) {
        cachedRewards = itemStack
    }

    override fun removeItem(i: Int, amount: Int): ItemStack {
        val split = cachedRewards.split(amount)
        if (!split.isEmpty) {
            this.setChanged()
        }

        return split
    }

    override fun removeItemNoUpdate(i: Int): ItemStack {
        val split = cachedRewards
        cachedRewards = ItemStack.EMPTY
        return split
    }

    override fun stillValid(player: Player) = level?.getBlockEntity(this.blockPos) == this

    fun getInputItem(i: Int): ItemStack = cachedInput[i]
    fun setInputItem(i: Int, itemStack: ItemStack) {
        cachedInput[i] = itemStack
        if (itemStack.count > MAX_STACK_SIZE) {
            itemStack.count = MAX_STACK_SIZE
        }
        this.setChanged()
    }

    val inputItemsEmpty get() = cachedInput.isEmpty() || cachedInput.all { itemStack -> itemStack.isEmpty }

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

            when (portal.blockState.getValue(MODE)) {
                ContractPortalMode.UNLIT -> {}
                ContractPortalMode.LIT -> {
                    if (portal.contractSlot.isEmpty) {
                        return false
                    }

                    val didConsume = tryConsume(portal)
                    val didSuck = suckInItems(level, portal)
                    val didUpdate = ContractItem.tick(portal.contractSlot)

                    if (didConsume || didSuck || didUpdate) {
                        portal.setCooldown(5)
                        setChanged(level, blockPos, blockState)
                        return true
                    } else {
                        portal.setCooldown(10)
                    }
                }

                ContractPortalMode.COIN -> {
                    val stackToSpit = if (!portal.cachedRewards.isEmpty) {
                        portal.cachedRewards
                    } else if (!portal.cachedInput.isEmpty()) {
                        portal.cachedInput.firstOrNull { itemStack -> !itemStack.isEmpty }
                    } else {
                        null
                    }

                    if (stackToSpit == null) {
                        level.setBlockAndUpdate(blockPos, blockState.setValue(MODE, ContractPortalMode.UNLIT))
                        level.gameEvent(null, GameEvent.BLOCK_CHANGE, blockPos)
                        level.sendBlockUpdated(blockPos, blockState, blockState, UPDATE_ALL)
                        return true
                    }

                    spitItemStack(stackToSpit, level, blockPos, false, max = sqrt(stackToSpit.count.toFloat()).toInt())
                    portal.setCooldown(10)
                    return true
                }

                else -> {}
            }

            return false
        }

        private fun spitItemStack(
            stackToSpit: ItemStack,
            level: Level,
            blockPos: BlockPos,
            denominate: Boolean,
            min: Int = 1,
            max: Int? = null,
        ) {
            // TO-DO: handle denominations
            val amountToSpit = level.random.nextIntBetweenInclusive(
                min(stackToSpit.maxStackSize, min(min, stackToSpit.count)),
                min(stackToSpit.maxStackSize, min(max ?: stackToSpit.maxStackSize, stackToSpit.count))
            )

            val splitStack = stackToSpit.split(amountToSpit)
            spawnItem(splitStack, level, blockPos)
        }

        private fun spawnItem(itemStack: ItemStack, level: Level, blockPos: BlockPos) {
            val itemEntity = ItemEntity(level, blockPos.x + 0.5, blockPos.y + 0.75, blockPos.z + 0.5, itemStack)
            val magnitude = 0.15
            val radians = level.random.nextDouble() * Math.PI * 2
            itemEntity.setDeltaMovement(sin(radians) * magnitude, magnitude * 3, cos(radians) * magnitude)
            level.addFreshEntity(itemEntity)
        }

        private fun getItemsAtAndAbove(level: Level, portal: ContractPortalBlockEntity): List<ItemEntity> {
            return getSuckShape().toAabbs().stream().flatMap { aABB: AABB ->
                level.getEntitiesOfClass(
                    ItemEntity::class.java,
                    aABB.move(portal.getLevelX() - 0.5, portal.getLevelY() - 0.5, portal.getLevelZ() - 0.5),
                    EntitySelector.ENTITY_STILL_ALIVE
                ).stream()
            }.collect(Collectors.toList())
        }

        private fun tryConsume(portal: ContractPortalBlockEntity): Boolean {
            val contractTag = ContractItem.getBaseTag(portal.contractSlot) ?: return false
            val rewardItem = contractTag.rewardItem ?: return false
            val matchingStacks = portal.cachedInput.filter { itemStack ->
                !itemStack.isEmpty && ContractItem.matches(
                    portal.contractSlot,
                    itemStack
                )
            }
            val matchingCount = matchingStacks.sumOf { itemStack -> itemStack.count }
            val unitCount = matchingCount / contractTag.countPerUnit.get()
            if (unitCount == 0) {
                return false
            }

            val goalAmount = unitCount * contractTag.countPerUnit.get()
            var amountTaken = 0
            for (itemStack in portal.cachedInput) {
                if (amountTaken >= goalAmount) {
                    break
                }

                if (itemStack.isEmpty) {
                    continue
                }

                if (ContractItem.matches(portal.contractSlot, itemStack)) {
                    val amountToTake = min(itemStack.count, goalAmount - amountTaken)
                    amountTaken += amountToTake
                    itemStack.shrink(amountToTake)
                }
            }

            val rewardsReceived = unitCount * contractTag.unitPrice.get()
            if (portal.cachedRewards.isEmpty) {
                portal.cachedRewards = ItemStack(rewardItem, rewardsReceived)
            } else {
                portal.cachedRewards.grow(rewardsReceived)
            }

            contractTag.quantityFulfilled.put(
                contractTag.quantityFulfilled.get() + amountTaken
            )
            contractTag.quantityFulfilledEver.put(
                contractTag.quantityFulfilledEver.get() + amountTaken
            )

            return true
        }

        private fun suckInItems(level: Level, portal: ContractPortalBlockEntity): Boolean {
            val aboveItems = getItemsAtAndAbove(level, portal).iterator()

            if (portal.inventoryFull()) {
                return false
            }

            while (aboveItems.hasNext()) {
                val itemEntity = aboveItems.next()

                if (ContractItem.matches(portal.contractSlot, itemEntity.item)) {
                    if (addInputItem(portal, itemEntity)) {
                        return true
                    }
                } else {
                    spitItemStack(itemEntity.item, level, portal.blockPos, false)
                }
            }

            return false
        }

        private fun addInputItem(portal: ContractPortalBlockEntity, itemEntity: ItemEntity): Boolean {
            var didConsumeAll = false
            val beforeStack = itemEntity.item.copy()
            val afterStack = addInputItem(portal, beforeStack)
            if (afterStack.isEmpty) {
                didConsumeAll = true
                itemEntity.discard()
            } else {
                itemEntity.item = afterStack
            }

            return didConsumeAll
        }

        private fun addInputItem(portal: ContractPortalBlockEntity, itemStack: ItemStack): ItemStack {
            var mutItemStack = itemStack
            val size = CONTAINER_SIZE

            var i = 0
            while (i < size && !mutItemStack.isEmpty) {
                mutItemStack = tryMoveInInputItem(portal, mutItemStack, i)
                ++i
            }

            return mutItemStack
        }

        private fun tryMoveInInputItem(portal: ContractPortalBlockEntity, itemStack: ItemStack, i: Int): ItemStack {
            var mutItemStack = itemStack
            val stackAtSlot = portal.getInputItem(i)
            var movedItems = false
            if (stackAtSlot.isEmpty) {
                portal.setInputItem(i, mutItemStack)
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
