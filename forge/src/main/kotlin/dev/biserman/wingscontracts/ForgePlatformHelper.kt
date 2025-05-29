package dev.biserman.wingscontracts

import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import platform.IPlatformHelper

class ForgePlatformHelper : IPlatformHelper {
    override fun getPortalItemHandler(portal: ContractPortalBlockEntity): ForgePortalItemHandler {
        return ForgePortalItemHandler(portal)
    }
}