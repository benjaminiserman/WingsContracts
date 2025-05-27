package dev.biserman.wingscontracts.client.ponder.scenes

import com.simibubi.create.foundation.ponder.CreateSceneBuilder
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.block.ContractPortalBlock
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import dev.biserman.wingscontracts.block.state.properties.ContractPortalMode
import dev.biserman.wingscontracts.client.ponder.ModPonderPlugin
import dev.biserman.wingscontracts.registry.ModBlockRegistry
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper
import net.createmod.ponder.api.scene.SceneBuilder
import net.createmod.ponder.api.scene.SceneBuildingUtil
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec3


object SpigotScene {
    fun register(helper: PonderSceneRegistrationHelper<ResourceLocation>) {
        helper.forComponents(ModBlockRegistry.CONTRACT_SPIGOT.id)
            .addStoryBoard(
                WingsContractsMod.prefix("contract_spigot/intro"),
                ::scene,
                ModPonderPlugin.CONTRACT_CATEGORY
            )
    }

    fun scene(builder: SceneBuilder, util: SceneBuildingUtil) {
        val scene = CreateSceneBuilder(builder)
        scene.title("contract_spigot.intro", "title")
        scene.configureBasePlate(0, 0, 5)
        scene.showBasePlate()
        scene.idle(10)
        scene.world().showSection(util.select().fromTo(0, 1, 0, 4, 3, 4), Direction.DOWN)

        val portal = util.grid().at(3, 1, 2)
        val spigot = util.grid().at(1, 3, 2)

        val contract = ModPonderPlugin.getExampleContract(scene.scene.world)

        scene.world().modifyBlock(
            portal,
            { it.setValue(ContractPortalBlock.MODE, ContractPortalMode.LIT) },
            false
        )
        scene.world().modifyBlockEntity<ContractPortalBlockEntity>(
            portal, ContractPortalBlockEntity::class.java
        ) {
            it.contractSlot = contract
        }

        scene.overlay()
            .showText(75)
            .text("text1")
            .attachKeyFrame()
            .pointAt(util.vector().topOf(spigot))
            .placeNearTarget()

        scene.idle(75)

        val droppedItems1 = listOf(
            ItemStack(Items.DIAMOND, 1),
            ItemStack(Items.DIAMOND, 1),
            ItemStack(Items.DIAMOND, 1),
            ItemStack(Items.DIAMOND, 1),
        )

        droppedItems1.forEach {
            var item = scene.world().createItemEntity(portal.above(2).center, Vec3.ZERO, it)
            scene.idle(10)
            scene.world().modifyEntity(item, Entity::discard)
        }

        scene.idle(10)

        scene.world().createItemEntity(spigot.below().center, Vec3.ZERO, ItemStack(Items.DIAMOND, 4))

        scene.idle(20)

        scene.overlay()
            .showText(75)
            .text("text2")
            .attachKeyFrame()
            .pointAt(util.vector().topOf(spigot.below(3)))
            .placeNearTarget()

        scene.idle(80)
    }
}