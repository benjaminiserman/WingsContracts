package dev.biserman.wingscontracts.neoforge.compat

import com.simibubi.create.foundation.data.CreateRegistrate
import net.neoforged.bus.api.IEventBus
import thedarkcolour.kotlinforforge.forge.MOD_BUS

class KotlinCreateRegistrate(modId: String) : CreateRegistrate(modId) {
    override fun getModEventBus(): IEventBus = MOD_BUS
}