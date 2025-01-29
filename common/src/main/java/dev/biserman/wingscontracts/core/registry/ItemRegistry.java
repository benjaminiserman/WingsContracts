package dev.biserman.wingscontracts.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.biserman.wingscontracts.WingsContractsMod;
import dev.biserman.wingscontracts.core.item.ContractItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(WingsContractsMod.MOD_ID,
            Registries.ITEM);

    public static final RegistrySupplier<Item> CONTRACT = ITEMS.register("contract",
            () -> new ContractItem(new Properties().stacksTo(1)));

    public static void register() {
        ITEMS.register();
    }
}
