package dev.biserman.wingscontracts.data

import dev.biserman.wingscontracts.api.AbyssalContract
import dev.biserman.wingscontracts.api.Contract
import dev.biserman.wingscontracts.api.Contract.Companion.id
import dev.biserman.wingscontracts.api.Contract.Companion.type
import dev.biserman.wingscontracts.tag.ContractTag
import dev.biserman.wingscontracts.tag.ContractTagHelper
import net.minecraft.world.item.ItemStack
import java.util.*

object LoadedContracts {
    private val contracts: MutableMap<UUID, Contract> = mutableMapOf()
    private val CONTRACT_LOAD_BY_TYPE = hashMapOf(
        1 to AbyssalContract::load,
    )

    operator fun get(id: UUID) = contracts[id]
    operator fun get(contractItemStack: ItemStack): Contract? {
        val contractTag = ContractTagHelper.getContractTag(contractItemStack) ?: return null
        return get(contractTag)
    }

    operator fun get(contractTag: ContractTag): Contract? {
        val id = contractTag.id ?: return null
        val cachedContract = contracts[contractTag.id]

        if (cachedContract == null) {
            val contract = CONTRACT_LOAD_BY_TYPE[contractTag.type ?: return null]?.invoke(contractTag) ?: return null
            contracts[id] = contract
            return contract
        }

        return cachedContract
    }

    fun update(contract: Contract) {
        contracts[contract.id] = contract
    }

    fun clear() = contracts.clear()
}