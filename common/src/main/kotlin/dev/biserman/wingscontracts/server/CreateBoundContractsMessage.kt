package dev.biserman.wingscontracts.server

import dev.architectury.networking.NetworkManager
import dev.architectury.networking.simple.BaseC2SMessage
import dev.architectury.networking.simple.MessageType
import dev.biserman.wingscontracts.core.BoundContract
import dev.biserman.wingscontracts.core.BoundContract.Companion.matchingContractId
import dev.biserman.wingscontracts.core.BoundContract.Companion.otherSideCountPerUnit
import dev.biserman.wingscontracts.core.BoundContract.Companion.otherSideTargets
import dev.biserman.wingscontracts.core.Contract.Companion.author
import dev.biserman.wingscontracts.core.Contract.Companion.countPerUnit
import dev.biserman.wingscontracts.core.Contract.Companion.id
import dev.biserman.wingscontracts.core.Contract.Companion.targetItems
import dev.biserman.wingscontracts.nbt.ContractTag
import dev.biserman.wingscontracts.registry.ModItemRegistry
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import java.util.*


class CreateBoundContractsMessage(val compoundTag: CompoundTag) : BaseC2SMessage() {
    constructor(buffer: FriendlyByteBuf) : this(buffer.readNbt() ?: CompoundTag())

    override fun getType(): MessageType = WingsContractsNetHandler.CREATE_BOUND_CONTRACTS

    override fun write(buffer: FriendlyByteBuf) {
        buffer.writeNbt(compoundTag)
    }

    override fun handle(context: NetworkManager.PacketContext) {
        if (context.player.mainHandItem.item == ModItemRegistry.BLANK_BOUND_CONTRACT.get()) {
            val leftContractTag = ContractTag(compoundTag.getCompound("Left"))
            val rightContractTag = ContractTag(compoundTag.getCompound("Right"))

            leftContractTag.otherSideTargets = rightContractTag.targetItems?.map { it.descriptionId }
            rightContractTag.otherSideTargets = leftContractTag.targetItems?.map { it.descriptionId }

            leftContractTag.otherSideCountPerUnit = rightContractTag.countPerUnit
            rightContractTag.otherSideCountPerUnit = leftContractTag.countPerUnit

            leftContractTag.id = UUID.randomUUID()
            rightContractTag.id = UUID.randomUUID()
            leftContractTag.matchingContractId = rightContractTag.id
            rightContractTag.matchingContractId = leftContractTag.id

            leftContractTag.author = context.player.name.string
            rightContractTag.author = context.player.name.string

            context.player.setItemInHand(context.player.usedItemHand, ItemStack.EMPTY)
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
