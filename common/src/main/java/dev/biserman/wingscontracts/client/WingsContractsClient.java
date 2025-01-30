package dev.biserman.wingscontracts.client;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.biserman.wingscontracts.client.renderer.ContractPortalBlockEntityRenderer;
import dev.biserman.wingscontracts.registry.BlockEntityRegistry;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public final class WingsContractsClient {
    public static void init() {
        BlockEntityRendererRegistry.register(BlockEntityRegistry.CONTRACT_PORTAL.get(),
                ContractPortalBlockEntityRenderer::new);
    }
}
