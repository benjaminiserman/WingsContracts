package dev.biserman.wingscontracts;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import dev.biserman.wingscontracts.client.WingsContractsClient;
import dev.biserman.wingscontracts.registry.BlockEntityRegistry;
import dev.biserman.wingscontracts.registry.BlockRegistry;
import dev.biserman.wingscontracts.registry.CommandRegistry;
import dev.biserman.wingscontracts.registry.ItemRegistry;

public final class WingsContractsMod {
    public static final String MOD_ID = "wingscontracts";

    public static void init() {
        BlockRegistry.register();
        ItemRegistry.register();
        BlockEntityRegistry.register();
        CommandRegistry.register();

        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            ClientLifecycleEvent.CLIENT_SETUP.register(instance -> WingsContractsClient.init());
        });
    }
}
