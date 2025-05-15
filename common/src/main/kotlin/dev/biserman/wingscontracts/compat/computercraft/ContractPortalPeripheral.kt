package dev.biserman.wingscontracts.compat.computercraft

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.peripheral.IPeripheral
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import dev.biserman.wingscontracts.compat.computercraft.DetailsHelper.details
import dev.biserman.wingscontracts.core.AbyssalContract
import dev.biserman.wingscontracts.data.LoadedContracts
import dev.biserman.wingscontracts.server.AvailableContractsData

class ContractPortalPeripheral(private val portal: ContractPortalBlockEntity) : IPeripheral {
    @Suppress("CovariantEquals")
    override fun equals(other: IPeripheral?) = other == this
    override fun getType() = "${WingsContractsMod.MOD_ID}.portal"

    val contract get() = LoadedContracts[portal.contractSlot]

    @LuaFunction
    fun getContractDetails(): Map<String, Any> = if (portal.contractSlot.isEmpty) {
        mapOf()
    } else {
        portal.contractSlot.details
    }

    @LuaFunction
    fun getCompletion() = {
        val abyssalContract = contract as? AbyssalContract
        abyssalContract?.let { it.unitsFulfilled.toDouble() / it.unitsDemanded } ?: -1
    }

    @LuaFunction
    fun getCachedRewardsDetails(): Map<String, Any> = if (portal.cachedRewards.isEmpty) {
        mapOf()
    } else {
        portal.cachedRewards.details
    }

    @LuaFunction
    fun getCachedInputDetails(): List<Map<String, Any>> = portal.cachedInput.filter { !it.isEmpty }.map { it.details }

    @LuaFunction
    fun getCachedRewardsDenominationDetails(): List<Map<String, Any>> =
        AvailableContractsData.get(portal.level!!).currencyHandler
            .denominateCurrency(portal.cachedRewards)
            .asSequence()
            .filter { !it.isEmpty }
            .map { it.details }
            .toList()
}