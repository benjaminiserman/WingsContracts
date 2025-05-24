package dev.biserman.wingscontracts.client.ponder.scenes

import com.simibubi.create.foundation.ponder.CreateSceneBuilder
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.block.ContractPortalBlock
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import dev.biserman.wingscontracts.block.state.properties.ContractPortalMode
import dev.biserman.wingscontracts.client.ponder.ModPonderPlugin
import dev.biserman.wingscontracts.registry.ModBlockRegistry
import dev.biserman.wingscontracts.registry.ModItemRegistry
import net.createmod.catnip.math.Pointing
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper
import net.createmod.ponder.api.scene.SceneBuilder
import net.createmod.ponder.api.scene.SceneBuildingUtil
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec3


object BoundContractScene {
    fun register(helper: PonderSceneRegistrationHelper<ResourceLocation>) {
        helper.forComponents(ModBlockRegistry.CONTRACT_PORTAL.id)
            .addStoryBoard(
                WingsContractsMod.prefix("contract_portal/bound_contract"),
                ::scene,
                ModPonderPlugin.CONTRACT_CATEGORY
            )
    }

    fun scene(builder: SceneBuilder, util: SceneBuildingUtil) {
        val scene = CreateSceneBuilder(builder)
        scene.title("contract_portal.bound_contract", "title")
        scene.configureBasePlate(0, 0, 5)
        scene.showBasePlate()
        scene.idle(10)
        scene.world().showSection(util.select().fromTo(0, 1, 0, 4, 1, 4), Direction.DOWN)

        val portal1Position = util.grid().at(3, 1, 2)
        val middlePosition = util.grid().at(2, 0, 2)
        val portal2Position = util.grid().at(1, 1, 2)

        scene.overlay()
            .showText(75)
            .text("text1")
            .attachKeyFrame()
            .placeNearTarget()

        scene.idle(75)

        scene.overlay()
            .showText(75)
            .text("text2")
            .attachKeyFrame()
            .pointAt(util.vector().topOf(middlePosition))
            .placeNearTarget()

        scene.idle(80)

        scene.overlay()
            .showControls(util.vector().topOf(middlePosition), Pointing.DOWN, 40)
            .rightClick()
            .withItem(ModItemRegistry.BLANK_BOUND_CONTRACT.get().defaultInstance)

        scene.idle(45)

        val contract1 = ModPonderPlugin.getExampleBoundContract1(scene.scene.world)
        val contract2 = ModPonderPlugin.getExampleBoundContract2(scene.scene.world)

        scene.overlay()
            .showControls(util.vector().topOf(portal1Position), Pointing.DOWN, 20)
            .rightClick()
            .withItem(contract1)
        scene.world().modifyBlockEntity<ContractPortalBlockEntity>(
            portal1Position, ContractPortalBlockEntity::class.java
        ) {
            it.contractSlot = contract1
        }
        scene.world().modifyBlock(
            portal1Position,
            { it.setValue(ContractPortalBlock.MODE, ContractPortalMode.LIT) },
            false
        )

        scene.idle(20)

        scene.overlay()
            .showControls(util.vector().topOf(portal2Position), Pointing.DOWN, 20)
            .rightClick()
            .withItem(contract1)
        scene.world().modifyBlockEntity<ContractPortalBlockEntity>(
            portal2Position, ContractPortalBlockEntity::class.java
        ) {
            it.contractSlot = contract2
        }
        scene.world().modifyBlock(
            portal2Position,
            { it.setValue(ContractPortalBlock.MODE, ContractPortalMode.LIT) },
            false
        )

        scene.idle(30)

        scene.overlay()
            .showText(75)
            .text("text3")
            .attachKeyFrame()
            .placeNearTarget()

        scene.idle(80)

        val droppedItems1 = listOf(
            ItemStack(Items.DIAMOND, 1),
            ItemStack(Items.DIAMOND, 1),
            ItemStack(Items.DIAMOND, 1),
            ItemStack(Items.DIAMOND, 1),
        )

        val droppedItems2 = listOf(
            ItemStack(Items.DIRT, 1),
            ItemStack(Items.DIRT, 1),
            ItemStack(Items.DIRT, 1),
            ItemStack(Items.DIRT, 1),
        )

        droppedItems1.forEach {
            var item = scene.world().createItemEntity(portal1Position.above(2).center, Vec3.ZERO, it)
            scene.idle(10)
            scene.world().modifyEntity(item, Entity::discard)
        }
        scene.idle(10)
        droppedItems2.forEach {
            var item = scene.world().createItemEntity(portal2Position.above(2).center, Vec3.ZERO, it)
            scene.idle(10)
            scene.world().modifyEntity(item, Entity::discard)
        }

        scene.overlay()
            .showControls(util.vector().topOf(portal1Position), Pointing.DOWN, 40)
            .rightClick()

        scene.idle(50)

        scene.world().modifyBlockEntity<ContractPortalBlockEntity>(
            portal1Position, ContractPortalBlockEntity::class.java
        ) {
            it.contractSlot = ItemStack.EMPTY
        }
        scene.world().modifyBlock(
            portal1Position,
            { it.setValue(ContractPortalBlock.MODE, ContractPortalMode.COIN) },
            false
        )

        droppedItems2.forEach {
            scene.idle(10)
            scene.world().createItemEntity(portal1Position.above().center, Vec3(-0.1, 0.45, 0.1), it)
        }
    }
}