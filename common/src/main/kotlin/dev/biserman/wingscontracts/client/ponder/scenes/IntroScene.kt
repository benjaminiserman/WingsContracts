package dev.biserman.wingscontracts.client.ponder.scenes

import com.simibubi.create.foundation.ponder.CreateSceneBuilder
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.block.ContractPortalBlock
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import dev.biserman.wingscontracts.block.state.properties.ContractPortalMode
import dev.biserman.wingscontracts.client.ponder.ModPonderPlugin
import dev.biserman.wingscontracts.command.LoadContractCommand
import dev.biserman.wingscontracts.registry.ModBlockRegistry
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


object IntroScene {
    fun register(helper: PonderSceneRegistrationHelper<ResourceLocation>) {
        helper.forComponents(ModBlockRegistry.CONTRACT_PORTAL.id)
            .addStoryBoard(
                WingsContractsMod.prefix("contract_portal/intro"),
                ::scene,
                ModPonderPlugin.CONTRACT_CATEGORY
            )
    }

    fun scene(builder: SceneBuilder, util: SceneBuildingUtil) {
        val scene = CreateSceneBuilder(builder)
        scene.title("contract_portal.intro", "title")
        scene.configureBasePlate(0, 0, 3)
        scene.showBasePlate()
        scene.idle(10)
        scene.world().showSection(util.select().fromTo(0, 1, 0, 2, 1, 2), Direction.DOWN)

        scene.overlay()
            .showText(100)
            .text("text_1")
            .attachKeyFrame()
            .pointAt(util.vector().topOf(1, 1, 1))
            .placeNearTarget()

        scene.idle(100)

        val contract = LoadContractCommand.loadContract(
            "{\"targetItems\": \"minecraft:diamond\",\"countPerUnit\": 8, \"reward\": 2}", scene.scene.world, "abyssal"
        ).createItem()
        scene.overlay()
            .showControls(util.vector().topOf(1, 1, 1), Pointing.DOWN, 40)
            .rightClick()
            .withItem(contract)
        scene.world().modifyBlockEntity<ContractPortalBlockEntity>(
            util.grid().at(1, 1, 1), ContractPortalBlockEntity::class.java
        ) {
            it.contractSlot = contract
        }
        scene.world().modifyBlock(
            util.grid().at(1, 1, 1),
            { it.setValue(ContractPortalBlock.MODE, ContractPortalMode.LIT) },
            false
        )

        scene.idle(75)

        scene.overlay()
            .showText(75)
            .text("text2")
            .attachKeyFrame()
            .pointAt(util.vector().topOf(1, 1, 1))
            .placeNearTarget()

        val droppedItems = listOf(
            ItemStack(Items.DIAMOND, 2),
            ItemStack(Items.DIAMOND, 2),
            ItemStack(Items.DIAMOND, 2),
            ItemStack(Items.DIAMOND, 2),
        )

        droppedItems.forEach {
            var item = scene.world().createItemEntity(util.vector().topOf(1, 2, 1), Vec3.ZERO, it)
            scene.idle(10)
            scene.world().modifyEntity(item, Entity::discard)
        }

        scene.idle(75)

        scene.overlay()
            .showText(75)
            .text("text2")
            .attachKeyFrame()
            .pointAt(util.vector().topOf(1, 1, 1))
            .placeNearTarget()

        scene.idle(75)

        scene.overlay()
            .showControls(util.vector().centerOf(1, 1, 1), Pointing.DOWN, 40)
            .rightClick()

        scene.world().modifyBlockEntity<ContractPortalBlockEntity>(
            util.grid().at(1, 1, 1), ContractPortalBlockEntity::class.java
        ) {
            it.contractSlot = ItemStack.EMPTY
        }
        scene.world().modifyBlock(
            util.grid().at(1, 1, 1),
            { it.setValue(ContractPortalBlock.MODE, ContractPortalMode.COIN) },
            false
        )

        scene.idle(50)

        scene.world().createItemEntity(util.vector().topOf(1, 1, 1), Vec3(-0.1, 0.45, 0.1), ItemStack(Items.EMERALD, 2))
    }
}