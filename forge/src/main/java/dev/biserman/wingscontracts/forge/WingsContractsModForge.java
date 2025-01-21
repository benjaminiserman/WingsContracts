package dev.biserman.wingscontracts.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import dev.biserman.wingscontracts.WingsContractsMod;

@Mod(WingsContractsMod.MOD_ID)
public final class WingsContractsModForge {
    public WingsContractsModForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(WingsContractsMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        WingsContractsMod.init();
    }
}
