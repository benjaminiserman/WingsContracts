package dev.biserman.wingscontracts.client

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry
import dev.biserman.wingscontracts.client.renderer.ContractPortalBlockEntityRenderer
import dev.biserman.wingscontracts.registry.BlockEntityRegistry
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider

@Environment(EnvType.CLIENT)
object WingsContractsClient {
    @JvmStatic
    fun init() {
        BlockEntityRendererRegistry.register(
            BlockEntityRegistry.CONTRACT_PORTAL.get()
        ) { context: BlockEntityRendererProvider.Context ->
            ContractPortalBlockEntityRenderer(
                context
            )
        }
    }
}
