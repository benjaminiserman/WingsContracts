package dev.biserman.wingscontracts.world

import dev.biserman.wingscontracts.api.AbyssalContract
import dev.biserman.wingscontracts.api.Contract
import dev.biserman.wingscontracts.api.Contract.Companion.type
import dev.biserman.wingscontracts.tag.ContractTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData

class ContractWorldData(
    private val contracts: MutableList<out Contract> = mutableListOf()
) : SavedData() {
    operator fun get(index: Int) = contracts[index]

    override fun save(compoundTag: CompoundTag): CompoundTag {
        val listTag = ListTag()
        contracts.forEach { listTag.add(it.save().tag) }
        compoundTag.put(CONTRACT_LIST, listTag)
        return compoundTag
    }

    companion object {
        private const val IDENTIFIER = "wingscontracts_world_data"
        private const val CONTRACT_LIST = "contractList"
        private val CONTRACT_LOAD_BY_TYPE = hashMapOf(
            1 to AbyssalContract::load,
        )

        fun load(nbt: CompoundTag): ContractWorldData {
            val contractList = nbt.getList(CONTRACT_LIST, 10)
            val contracts = contractList
                .filterIsInstance<CompoundTag>()
                .map { ContractTag(it) }
                .mapNotNull { CONTRACT_LOAD_BY_TYPE[it.type]?.invoke(it) }
                .toMutableList()

            return ContractWorldData(contracts)
        }

        fun get(world: Level): ContractWorldData? {
            if (world !is ServerLevel) {
                return null;
            }

            val data = world.server.getLevel(Level.OVERWORLD)?.dataStorage?.computeIfAbsent(
                ContractWorldData::load,
                ::ContractWorldData,
                IDENTIFIER
            )

            return data
        }
    }
}