package dev.biserman.wingscontracts.core.registry;

import java.util.function.Supplier;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.biserman.wingscontracts.WingsContractsMod;
import dev.biserman.wingscontracts.core.block.ContractPortalBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(WingsContractsMod.MOD_ID,
            Registries.BLOCK);

    public static final RegistrySupplier<Block> CONTRACT_PORTAL = registerBlockWithItem("contract_portal",
            () -> new ContractPortalBlock(BlockBehaviour.Properties.copy(Blocks.ENCHANTING_TABLE)
                    .lightLevel(ContractPortalBlock::getLightLevel)));

    public static <T extends Block> RegistrySupplier<T> registerBlockWithItem(String name, Supplier<T> block) {
        var toReturn = BLOCKS.register(name, block);
        ItemRegistry.ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties()));
        return toReturn;
    }

    public static void register() {
        BLOCKS.register();
    }
}
