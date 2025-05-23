package dev.biserman.wingscontracts.block.state.properties

import net.minecraft.util.StringRepresentable

enum class ContractPortalMode(key: String, i: Int) : StringRepresentable {
    UNLIT("UNLIT", 0),
    LIT("LIT", 1),
    COIN("COIN", 2),
    NOT_CONNECTED("NOT_CONNECTED", 3),
    ERROR("ERROR", 4);

    override fun toString(): String {
        return this.name.lowercase()
    }

    override fun getSerializedName(): String {
        return this.name.lowercase()
    }
}
