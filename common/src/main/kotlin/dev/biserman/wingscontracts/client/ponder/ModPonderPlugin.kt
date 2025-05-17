package dev.biserman.wingscontracts.client.ponder

import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.client.ponder.scenes.IntroScene
import dev.biserman.wingscontracts.client.ponder.scenes.RedstoneInputScene
import dev.biserman.wingscontracts.client.ponder.scenes.RedstoneOutputScene
import dev.biserman.wingscontracts.client.ponder.scenes.WoolScene
import dev.biserman.wingscontracts.command.LoadContractCommand
import dev.biserman.wingscontracts.registry.ModBlockRegistry
import net.createmod.ponder.api.registration.PonderPlugin
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

object ModPonderPlugin : PonderPlugin {
    val CONTRACT_CATEGORY = WingsContractsMod.prefix("contract_portal")

    fun getExampleContract(level: Level) = LoadContractCommand.loadContract(
        "{\"targetItems\":\"minecraft:diamond\",\"countPerUnit\": 8,\"reward\":{\"Count\": 1,\"id\":\"minecraft:emerald\"}}",
        level,
        "abyssal"
    ).createItem()

    override fun getModId() = WingsContractsMod.MOD_ID

    override fun registerScenes(helper: PonderSceneRegistrationHelper<ResourceLocation>) {
        IntroScene.register(helper)
        WoolScene.register(helper)
        RedstoneInputScene.register(helper)
        RedstoneOutputScene.register(helper)
    }

    override fun registerTags(helper: PonderTagRegistrationHelper<ResourceLocation?>) {
        helper.registerTag(CONTRACT_CATEGORY)
            .addToIndex()
            .item(ModBlockRegistry.CONTRACT_PORTAL.get()!!, true, false)
            .register()

        helper.addToTag(CONTRACT_CATEGORY)
            .add(ModBlockRegistry.CONTRACT_PORTAL.id)
    }
}