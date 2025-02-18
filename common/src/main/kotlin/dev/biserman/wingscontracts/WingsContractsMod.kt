package dev.biserman.wingscontracts

import dev.architectury.event.events.client.ClientLifecycleEvent
import dev.architectury.utils.Env
import dev.architectury.utils.EnvExecutor
import dev.biserman.wingscontracts.client.WingsContractsClient
import dev.biserman.wingscontracts.registry.BlockEntityRegistry
import dev.biserman.wingscontracts.registry.BlockRegistry
import dev.biserman.wingscontracts.registry.CommandRegistry
import dev.biserman.wingscontracts.registry.ItemRegistry
import org.apache.logging.log4j.LogManager

object WingsContractsMod {
    const val MOD_ID: String = "wingscontracts"
    val LOGGER = LogManager.getLogger("WingsContracts")

    fun init() {
        BlockRegistry.register()
        ItemRegistry.register()
        BlockEntityRegistry.register()
        CommandRegistry.register()

        EnvExecutor.runInEnv(Env.CLIENT) {
            Runnable {
                ClientLifecycleEvent.CLIENT_SETUP.register(ClientLifecycleEvent.ClientState { WingsContractsClient.init() })
            }
        }
    }
}
