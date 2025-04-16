package dev.biserman.wingscontracts.server

import dev.architectury.networking.NetworkManager
import dev.architectury.networking.simple.BaseS2CMessage
import dev.architectury.networking.simple.MessageType
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerLevel


class SyncAvailableContractsMessage(val compoundTag: CompoundTag) : BaseS2CMessage() {
    constructor(level: ServerLevel) : this(AvailableContractsData.get(level).save(CompoundTag()))
    constructor(buffer: FriendlyByteBuf) : this(buffer.readNbt()!!)

    override fun getType(): MessageType = WingsContractsNetHandler.SYNC_AVAILABLE_CONTRACTS

    override fun write(buffer: FriendlyByteBuf) {
        buffer.writeNbt(compoundTag)
    }

    override fun handle(context: NetworkManager.PacketContext) {
        AvailableContractsData.set(context.player.level(), AvailableContractsData.load(compoundTag))
    }
}