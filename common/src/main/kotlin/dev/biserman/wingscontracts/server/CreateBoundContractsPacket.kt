package dev.biserman.wingscontracts.server

import dev.architectury.networking.NetworkManager
import dev.biserman.wingscontracts.WingsContractsMod.prefix
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
import io.netty.buffer.ByteBuf
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import java.util.*


class CreateBoundContractsPacket(val tag: CompoundTag) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<CreateBoundContractsPacket> = PACKET_ID

    companion object {
        val ID = prefix("create_bound_contracts")
        val PACKET_ID = CustomPacketPayload.Type<CreateBoundContractsPacket>(ID)
        val PACKET_CODEC: StreamCodec<ByteBuf, CreateBoundContractsPacket> =
            ByteBufCodecs.COMPOUND_TAG.map(::CreateBoundContractsPacket) { it.tag }

        fun handle(payload: CreateBoundContractsPacket, context: NetworkManager.PacketContext) {
            if (context.player.mainHandItem.item == ModItemRegistry.BLANK_BOUND_CONTRACT.get()) {
                val leftContractTag = ContractTag(payload.tag.getCompound("Left"))
                val rightContractTag = ContractTag(payload.tag.getCompound("Right"))

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
}
