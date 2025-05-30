package dev.biserman.wingscontracts.fabric

import dev.biserman.wingscontracts.WingsContractsMod
import net.fabricmc.api.ModInitializer

object WingsContractsModFabric : ModInitializer {
    override fun onInitialize() {
        WingsContractsMod.init()

//        EnvExecutor.runInEnv(Env.CLIENT) {
//            Runnable {
//                BuiltinItemRendererRegistry.INSTANCE.register(ModItemRegistry.ABYSSAL_CONTRACT.get(), ContractItemRenderer())
//                BuiltinItemRendererRegistry.INSTANCE.register(ModItemRegistry.BOUND_CONTRACT.get(), ContractItemRenderer())
//            }
//        }
    }
}
