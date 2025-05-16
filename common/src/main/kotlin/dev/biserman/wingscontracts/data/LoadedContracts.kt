package dev.biserman.wingscontracts.data

import dev.biserman.wingscontracts.core.AbyssalContract
import dev.biserman.wingscontracts.core.BoundContract
import dev.biserman.wingscontracts.core.Contract
import dev.biserman.wingscontracts.core.Contract.Companion.id
import dev.biserman.wingscontracts.core.Contract.Companion.type
import dev.biserman.wingscontracts.nbt.ContractTag
import dev.biserman.wingscontracts.nbt.ContractTagHelper
import net.minecraft.world.item.ItemStack
import java.util.*

object LoadedContracts {
    private val contracts: MutableMap<UUID, Contract> = mutableMapOf()
    private val CONTRACT_LOAD_BY_TYPE = hashMapOf(
        1 to AbyssalContract::load,
        2 to BoundContract::load
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
            val contract = CONTRACT_LOAD_BY_TYPE[contractTag.type ?: return null]
                ?.invoke(contractTag, null) ?: return null
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