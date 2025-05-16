package dev.biserman.wingscontracts.server

import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.networking.simple.SimpleNetworkManager
import dev.biserman.wingscontracts.WingsContractsMod
import net.minecraft.server.level.ServerLevel

object WingsContractsNetHandler {
    val NET = SimpleNetworkManager.create(WingsContractsMod.MOD_ID)!!

    val SYNC_AVAILABLE_CONTRACTS = NET.registerS2C("sync_available_contracts", ::SyncAvailableContractsMessage)!!
    val CREATE_BOUND_CONTRACTS = NET.registerC2S("create_bound_contracts", ::CreateBoundContractsMessage)!!

    fun init() {
        PlayerEvent.PLAYER_JOIN.register { player ->
            SyncAvailableContractsMessage(player.level() as ServerLevel).sendTo(
                player
            )
        }
    }
}