package dev.biserman.wingscontracts

import dev.architectury.event.events.client.ClientLifecycleEvent
import dev.architectury.event.events.common.TickEvent
import dev.architectury.platform.Platform
import dev.architectury.utils.Env
import dev.architectury.utils.EnvExecutor
import dev.biserman.wingscontracts.advancements.ContractCompleteTrigger
import dev.biserman.wingscontracts.client.WingsContractsClient
import dev.biserman.wingscontracts.client.ponder.ModPonderPlugin
import dev.biserman.wingscontracts.compat.CompatMods
import dev.biserman.wingscontracts.compat.computercraft.ModItemDetailProvider
import dev.biserman.wingscontracts.registry.*
import dev.biserman.wingscontracts.server.AvailableContractsData
import dev.biserman.wingscontracts.server.WingsContractsNetHandler
import net.createmod.ponder.foundation.PonderIndex
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.script.ScriptEngineManager
import javax.script.SimpleScriptContext

object WingsContractsMod {
    const val MOD_ID: String = "wingscontracts"
    val LOGGER: Logger = LogManager.getLogger("WingsContracts")
    val JS = ScriptEngineManager().getEngineByName("javascript")
    val JS_CONTEXT = SimpleScriptContext()

    fun init() {
        ModBlockRegistry.register()
        ModItemRegistry.register()
        ModBlockEntityRegistry.register()
        ModSoundRegistry.register()
        ModCommandRegistry.register()
        ModMenuRegistry.register()
        ModReloadListenerRegistry.register()
        CriteriaTriggers.register(ContractCompleteTrigger.INSTANCE)

        WingsContractsNetHandler.init()

        if (Platform.isModLoaded(CompatMods.COMPUTERCRAFT)) {
            ModItemDetailProvider.register()
        }

        TickEvent.Server.SERVER_LEVEL_POST.register { level ->
            AvailableContractsData.get(level).serverTick(level)
        }

        EnvExecutor.runInEnv(Env.CLIENT) {
            Runnable {
                ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register { level ->
                    AvailableContractsData.fakeData =
                        AvailableContractsData()
                }

                ClientLifecycleEvent.CLIENT_SETUP.register(ClientLifecycleEvent.ClientState {
                    WingsContractsClient.init()
                    ModMenuRegistry.clientsideRegister()
                    if (Platform.isModLoaded("create")) {
                        PonderIndex.addPlugin(ModPonderPlugin)
                    }
                })
            }
        }
    }

    fun prefix(path: String) = ResourceLocation("$MOD_ID:$path")
}
