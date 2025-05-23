package dev.biserman.wingscontracts.client

import dev.architectury.registry.client.level.entity.EntityRendererRegistry
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry
import dev.biserman.wingscontracts.client.renderer.ContractPortalBlockEntityRenderer
import dev.biserman.wingscontracts.client.renderer.FakeItemEntityRenderer
import dev.biserman.wingscontracts.registry.ModBlockEntityRegistry
import dev.biserman.wingscontracts.registry.ModEntityRegistry
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider

@Environment(EnvType.CLIENT)
object WingsContractsClient {
    @JvmStatic
    fun init() {
        BlockEntityRendererRegistry.register(
            ModBlockEntityRegistry.CONTRACT_PORTAL.get()
        ) { context: BlockEntityRendererProvider.Context ->
            ContractPortalBlockEntityRenderer(
                context
            )
        }

        EntityRendererRegistry.register(
            ModEntityRegistry.FAKE_ITEM
        ) { FakeItemEntityRenderer(it) }
    }
}
