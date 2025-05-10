package dev.biserman.wingscontracts.client.ponder.scenes

import com.simibubi.create.foundation.ponder.CreateSceneBuilder
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.client.ponder.ModPonderPlugin
import dev.biserman.wingscontracts.registry.ModBlockRegistry
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper
import net.createmod.ponder.api.scene.SceneBuilder
import net.createmod.ponder.api.scene.SceneBuildingUtil
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation


object WoolScene {
    fun register(helper: PonderSceneRegistrationHelper<ResourceLocation>) {
        helper.forComponents(ModBlockRegistry.CONTRACT_PORTAL.id)
            .addStoryBoard(
                WingsContractsMod.prefix("contract_portal/wool"),
                ::scene,
                ModPonderPlugin.CONTRACT_CATEGORY
            )
    }

    fun scene(builder: SceneBuilder, util: SceneBuildingUtil) {
        val scene = CreateSceneBuilder(builder)
        scene.title("contract_portal.wool", "title")
        scene.configureBasePlate(0, 0, 3)
        scene.showBasePlate()
        scene.idle(10)
        scene.world().showSection(util.select().fromTo(0, 1, 0, 2, 1, 2), Direction.DOWN)
        scene.idle(10)
        scene.world().showSection(util.select().fromTo(0, 2, 0, 2, 2, 2), Direction.DOWN)

        scene.overlay()
            .showText(100)
            .text("text_1")
            .attachKeyFrame()
            .pointAt(util.vector().topOf(1, 1, 1))
            .placeNearTarget()

        scene.idle(100)
    }
}