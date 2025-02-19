package dev.biserman.wingscontracts.compat.computercraft.peripherals

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.peripheral.IPeripheral
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import dev.biserman.wingscontracts.compat.computercraft.peripherals.DetailsHelper.details
import dev.biserman.wingscontracts.config.DenominatedCurrenciesHandler
import dev.biserman.wingscontracts.data.LoadedContracts

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
    fun getCompletion() = contract?.let { it.unitsFulfilled.toDouble() / it.unitsDemanded }

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
        DenominatedCurrenciesHandler
            .denominateCurrency(portal.cachedRewards)
            .asSequence()
            .filter { !it.isEmpty }
            .map { it.details }
            .toList()
}