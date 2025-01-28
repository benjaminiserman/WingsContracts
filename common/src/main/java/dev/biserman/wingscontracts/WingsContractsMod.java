package dev.biserman.wingscontracts;

import dev.biserman.wingscontracts.core.registry.BlockEntityRegistry;
import dev.biserman.wingscontracts.core.registry.BlockRegistry;
import dev.biserman.wingscontracts.core.registry.ItemRegistry;

public final class WingsContractsMod {
    public static final String MOD_ID = "wingscontracts";

    public static void init() {
        BlockRegistry.register();
        ItemRegistry.register();
        BlockEntityRegistry.register();
    }
}
