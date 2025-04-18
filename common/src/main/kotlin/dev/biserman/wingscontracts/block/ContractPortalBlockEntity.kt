package dev.biserman.wingscontracts.block

import dev.biserman.wingscontracts.api.Contract
import dev.biserman.wingscontracts.block.ContractPortalBlock.Companion.MODE
import dev.biserman.wingscontracts.block.state.properties.ContractPortalMode
import dev.biserman.wingscontracts.data.LoadedContracts
import dev.biserman.wingscontracts.registry.ModBlockEntityRegistry
import dev.biserman.wingscontracts.registry.ModBlockRegistry
import dev.biserman.wingscontracts.registry.ModMenuRegistry
import dev.biserman.wingscontracts.registry.ModSoundRegistry
import dev.biserman.wingscontracts.server.AvailableContractsData
import dev.biserman.wingscontracts.tag.ContractTag
import dev.biserman.wingscontracts.tag.ContractTagHelper
import net.minecraft.core.BlockPos
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.BlockTags
import net.minecraft.util.Mth.*
import net.minecraft.world.ContainerHelper
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
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
import kotlin.math.max
import kotlin.math.min

class ContractPortalBlockEntity(
    blockPos: BlockPos,
    blockState: BlockState,
) :
    BlockEntity(ModBlockEntityRegistry.CONTRACT_PORTAL.get(), blockPos, blockState), MenuProvider {
    var cooldownTime: Int
    var contractSlot: ItemStack
    var cachedRewards: ItemStack
    var cachedInput: NonNullList<ItemStack>

    private fun getLevelX(): Double = worldPosition.x.toDouble() + 0.5
    private fun getLevelY(): Double = worldPosition.y.toDouble() + 0.5
    private fun getLevelZ(): Double = worldPosition.z.toDouble() + 0.5

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

    private fun getInputItem(i: Int): ItemStack = cachedInput[i]
    private fun setInputItem(i: Int, itemStack: ItemStack) {
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
                    val contractTag = ContractTagHelper.getContractTag(portal.contractSlot) ?: return false
                    val contract = LoadedContracts[contractTag] ?: return false

                    if (level.hasNeighborSignal(blockPos)) {
                        level.setBlockAndUpdate(blockPos, blockState.setValue(MODE, ContractPortalMode.COIN))
                        level.gameEvent(null, GameEvent.BLOCK_CHANGE, blockPos)
                        level.sendBlockUpdated(blockPos, blockState, blockState, UPDATE_ALL)
                        return true
                    }

                    val didConsume = portal.tryConsume(contract, contractTag)
                    val didSuck = portal.suckInItems(contract, level)
                    val didUpdate = contract.tryUpdateTick(contractTag)

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
                    } else if (portal.contractSlot.isEmpty && !portal.cachedInput.isEmpty()) {
                        portal.cachedInput.firstOrNull { itemStack -> !itemStack.isEmpty }
                    } else {
                        null
                    }

                    if (stackToSpit == null) {
                        if (level.hasNeighborSignal(blockPos)) {
                            return false
                        }

                        level.setBlockAndUpdate(
                            blockPos, blockState.setValue(
                                MODE, if (portal.contractSlot.isEmpty) {
                                    ContractPortalMode.UNLIT
                                } else {
                                    ContractPortalMode.LIT
                                }
                            )
                        )
                        level.gameEvent(null, GameEvent.BLOCK_CHANGE, blockPos)
                        level.sendBlockUpdated(blockPos, blockState, blockState, UPDATE_ALL)
                        return true
                    }

                    val currencyHandler = AvailableContractsData.get(level).currencyHandler
                    if (currencyHandler.isCurrency(stackToSpit)) {
                        val denominatedStack = currencyHandler.splitHighestDenomination(stackToSpit)
                        spitItemStack(denominatedStack, level, blockPos, amountToSpit = denominatedStack.count)
                    } else {
                        spitItemStack(stackToSpit, level, blockPos, amountToSpit = max(4, stackToSpit.count / 2))
                    }

                    playSound(level, blockPos, ModSoundRegistry.PORTAL_SPIT.get())

                    portal.setCooldown(10)
                    return true
                }

                else -> {}
            }

            return false
        }

        private fun playSound(level: Level, blockPos: BlockPos, sound: SoundEvent) {
            if (!level.getBlockState(blockPos.below()).`is`(BlockTags.WOOL)) {
                level.playSound(null, blockPos, sound, SoundSource.BLOCKS)
            }
        }

        private fun spitItemStack(
            stackToSpit: ItemStack,
            level: Level,
            blockPos: BlockPos,
            amountToSpit: Int
        ) {
            val splitStack = stackToSpit.split(min(min(amountToSpit, stackToSpit.count), stackToSpit.maxStackSize))
            spawnItem(splitStack, level, blockPos)
        }

        private fun spawnItem(itemStack: ItemStack, level: Level, blockPos: BlockPos) {
            val itemEntity = ItemEntity(level, blockPos.x + 0.5, blockPos.y + 0.75, blockPos.z + 0.5, itemStack)
            val magnitude = 0.15
            val radians = level.random.nextFloat() * PI * 2
            itemEntity.setDeltaMovement(sin(radians) * magnitude, magnitude * 3, cos(radians) * magnitude)
            level.addFreshEntity(itemEntity)
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

    private fun suckInItems(contract: Contract, level: Level): Boolean {
        val aboveItems = getItemsAtAndAbove(level).iterator()

        if (inventoryFull()) {
            return false
        }

        while (aboveItems.hasNext()) {
            val itemEntity = aboveItems.next()

            if (contract.matches(itemEntity.item)) {
                if (addInputItem(itemEntity)) {
                    playSound(level, blockPos, ModSoundRegistry.PORTAL_ACCEPT.get())
                    return true
                }
            } else {
                spitItemStack(itemEntity.item, level, blockPos, itemEntity.item.count)
                playSound(level, blockPos, ModSoundRegistry.PORTAL_REJECT.get())
            }
        }

        return false
    }

    private fun addInputItem(itemEntity: ItemEntity): Boolean {
        var didConsumeAll = false
        val beforeStack = itemEntity.item.copy()
        val afterStack = addInputItem(beforeStack)
        if (afterStack.isEmpty) {
            didConsumeAll = true
            itemEntity.discard()
        } else {
            itemEntity.item = afterStack
        }

        return didConsumeAll
    }

    private fun addInputItem(itemStack: ItemStack): ItemStack {
        var mutItemStack = itemStack
        val size = CONTAINER_SIZE

        var i = 0
        while (i < size && !mutItemStack.isEmpty) {
            mutItemStack = tryMoveInInputItem(mutItemStack, i)
            ++i
        }

        return mutItemStack
    }

    private fun tryMoveInInputItem(itemStack: ItemStack, i: Int): ItemStack {
        var mutItemStack = itemStack
        val stackAtSlot = getInputItem(i)
        var movedItems = false
        if (stackAtSlot.isEmpty) {
            setInputItem(i, mutItemStack)
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
            setCooldown(10)
        }

        setChanged()

        return mutItemStack
    }

    private fun tryConsume(contract: Contract, contractTag: ContractTag): Boolean {
        val unitsConsumed = contract.tryConsumeFromItems(contractTag, cachedInput)

        if (unitsConsumed == 0) {
            return false
        }

        val rewards = contract.getRewardsForUnits(unitsConsumed)

        if (cachedRewards.isEmpty || rewards.item != cachedRewards.item) {
            cachedRewards = rewards
        } else {
            cachedRewards.grow(rewards.count)
        }

        return true
    }

    private fun getItemsAtAndAbove(level: Level): List<ItemEntity> {
        return getSuckShape().toAabbs().stream().flatMap { aABB: AABB ->
            level.getEntitiesOfClass(
                ItemEntity::class.java,
                aABB.move(getLevelX() - 0.5, getLevelY() - 0.5, getLevelZ() - 0.5),
                EntitySelector.ENTITY_STILL_ALIVE
            ).stream()
        }.collect(Collectors.toList())
    }

    override fun getDisplayName(): Component? = ModBlockRegistry.CONTRACT_PORTAL.get()?.name

    override fun createMenu(
        i: Int,
        inventory: Inventory,
        player: Player
    ): AbstractContainerMenu = ModMenuRegistry.CONTRACT_PORTAL.get().create(i, inventory)!!
}
