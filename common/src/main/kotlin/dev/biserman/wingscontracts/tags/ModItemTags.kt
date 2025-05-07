package dev.biserman.wingscontracts.tags

import dev.biserman.wingscontracts.WingsContractsMod.prefix
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey

object ModItemTags {
    @JvmField val CONTRACTS = TagKey.create(Registries.ITEM, prefix("contracts"))
}