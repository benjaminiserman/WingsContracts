package dev.biserman.wingscontracts.server

import dev.architectury.networking.NetworkManager
import dev.architectury.networking.simple.BaseS2CMessage
import dev.architectury.networking.simple.MessageType
import dev.biserman.wingscontracts.data.ContractSavedData
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerLevel


class SyncAvailableContractsMessage(val compoundTag: CompoundTag) : BaseS2CMessage() {
    constructor(level: ServerLevel) : this(ContractSavedData.get(level).save(CompoundTag()))
    constructor(buffer: FriendlyByteBuf) : this(buffer.readNbt() ?: CompoundTag())

    override fun getType(): MessageType = WingsContractsNetHandler.SYNC_AVAILABLE_CONTRACTS

    override fun write(buffer: FriendlyByteBuf) {
        buffer.writeNbt(compoundTag)
    }

    override fun handle(context: NetworkManager.PacketContext) {
        ContractSavedData.set(context.player.level(), ContractSavedData.load(compoundTag))
    }
}