package dev.biserman.wingscontracts.server

import dev.architectury.networking.NetworkManager
import dev.architectury.networking.simple.BaseC2SMessage
import dev.architectury.networking.simple.MessageType
import dev.biserman.wingscontracts.core.BoundContract
import dev.biserman.wingscontracts.core.BoundContract.Companion.matchingContractId
import dev.biserman.wingscontracts.core.Contract.Companion.countPerUnit
import dev.biserman.wingscontracts.core.Contract.Companion.id
import dev.biserman.wingscontracts.core.Contract.Companion.name
import dev.biserman.wingscontracts.core.Contract.Companion.targetItems
import dev.biserman.wingscontracts.nbt.ContractTag
import dev.biserman.wingscontracts.registry.ModItemRegistry
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import java.util.*


class CreateBoundContractsMessage(val compoundTag: CompoundTag) : BaseC2SMessage() {
    constructor(buffer: FriendlyByteBuf) : this(buffer.readNbt()!!)

    override fun getType(): MessageType = WingsContractsNetHandler.SYNC_AVAILABLE_CONTRACTS

    override fun write(buffer: FriendlyByteBuf) {
        buffer.writeNbt(compoundTag)
    }

    override fun handle(context: NetworkManager.PacketContext) {
        if (context.player.mainHandItem.item == ModItemRegistry.BLANK_BOUND_CONTRACT.get()) {
            val leftContractTag = ContractTag(CompoundTag())
            val rightContractTag = ContractTag(CompoundTag())

            val leftItems = NonNullList.create<ItemStack>()
            ContainerHelper.loadAllItems(compoundTag.getCompound("Left"), leftItems)
            leftContractTag.targetItems = leftItems.filter { !it.isEmpty }.map { it.item }.distinct()
            val rightItems = NonNullList.create<ItemStack>()
            ContainerHelper.loadAllItems(compoundTag.getCompound("Right"), rightItems)
            rightContractTag.targetItems = rightItems.filter { !it.isEmpty }.map { it.item }.distinct()

            leftContractTag.countPerUnit = compoundTag.getCompound("Left").getInt("Count")
            rightContractTag.countPerUnit = compoundTag.getCompound("Right").getInt("Count")

            leftContractTag.name = compoundTag.getString("Name")
            rightContractTag.name = compoundTag.getString("Name")

            leftContractTag.id = UUID.randomUUID()
            rightContractTag.id = UUID.randomUUID()
            leftContractTag.matchingContractId = rightContractTag.id
            rightContractTag.matchingContractId = leftContractTag.id

            addItem(context.player, BoundContract.load(leftContractTag).createItem())
            addItem(context.player, BoundContract.load(rightContractTag).createItem())
        }
    }

    fun addItem(player: Player, itemStack: ItemStack) {
        val didAdd = player.inventory.add(itemStack)
        if (!didAdd) {
            val itemEntity = player.drop(itemStack, false)
            if (itemEntity != null) {
                itemEntity.setNoPickUpDelay()
                itemEntity.setTarget(player.uuid)
            }
        }
    }
}
