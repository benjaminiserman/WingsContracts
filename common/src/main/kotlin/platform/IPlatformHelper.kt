package platform

import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import dev.biserman.wingscontracts.container.ISidedPortalItemHandler

interface IPlatformHelper {
    fun getPortalItemHandler(portal: ContractPortalBlockEntity): ISidedPortalItemHandler
}