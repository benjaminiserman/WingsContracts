package dev.biserman.wingscontracts.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.block.ContractPortalBlock
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Supplier

@Suppress("MemberVisibilityCanBePrivate")
object ModBlockRegistry {
    val BLOCKS: DeferredRegister<Block> = DeferredRegister.create(
        WingsContractsMod.MOD_ID,
        Registries.BLOCK
    )

    val CONTRACT_PORTAL: RegistrySupplier<Block?> = registerBlockWithItem(
        "contract_portal"
    ) {
        ContractPortalBlock(
            BlockBehaviour.Properties.copy(Blocks.ENCHANTING_TABLE)
                .lightLevel { state: BlockState ->
                    ContractPortalBlock.getLightLevel(
                        state
                    )
                }
        )
    }

    private fun <T : Block?> registerBlockWithItem(name: String?, block: Supplier<T>?): RegistrySupplier<T> {
        val toReturn = BLOCKS.register(name, block)
        ModItemRegistry.ITEMS.register(
            name
        ) { BlockItem(toReturn.get()!!, Item.Properties()) }
        return toReturn
    }

    @JvmStatic
    fun register() {
        BLOCKS.register()
    }
}
