package dev.biserman.wingscontracts

import dev.architectury.event.events.client.ClientLifecycleEvent
import dev.architectury.platform.Platform
import dev.architectury.utils.Env
import dev.architectury.utils.EnvExecutor
import dev.biserman.wingscontracts.client.WingsContractsClient
import dev.biserman.wingscontracts.compat.CompatMods
import dev.biserman.wingscontracts.compat.computercraft.ModItemDetailProvider
import dev.biserman.wingscontracts.registry.*
import org.apache.logging.log4j.LogManager

object WingsContractsMod {
    const val MOD_ID: String = "wingscontracts"
    val LOGGER = LogManager.getLogger("WingsContracts")

    fun init() {
        ModBlockRegistry.register()
        ModItemRegistry.register()
        ModBlockEntityRegistry.register()
        ModSoundRegistry.register()
        ModCommandRegistry.register()

        if (Platform.isModLoaded(CompatMods.COMPUTERCRAFT)) {
            ModItemDetailProvider.register()
        }

        EnvExecutor.runInEnv(Env.CLIENT) {
            Runnable {
                ClientLifecycleEvent.CLIENT_SETUP.register(ClientLifecycleEvent.ClientState { WingsContractsClient.init() })
            }
        }
    }
}
