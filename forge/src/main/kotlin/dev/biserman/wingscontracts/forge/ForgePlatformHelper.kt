package dev.biserman.wingscontracts.forge

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import platform.IPlatformHelper

class ForgePlatformHelper : IPlatformHelper {
    override fun getPortalItemHandler(portal: ContractPortalBlockEntity): ForgePortalItemHandler {
        WingsContractsMod.LOGGER.info("getting portal item handler for $portal")
        return ForgePortalItemHandler(portal)
    }
}