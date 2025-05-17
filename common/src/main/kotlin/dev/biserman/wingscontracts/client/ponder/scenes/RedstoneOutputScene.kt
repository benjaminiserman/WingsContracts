package dev.biserman.wingscontracts.client.ponder.scenes

import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlockEntity
import com.simibubi.create.foundation.ponder.CreateSceneBuilder
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.block.ContractPortalBlock
import dev.biserman.wingscontracts.block.ContractPortalBlockEntity
import dev.biserman.wingscontracts.block.state.properties.ContractPortalMode
import dev.biserman.wingscontracts.client.ponder.ModPonderPlugin
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
import net.minecraft.world.level.block.ComparatorBlock
import net.minecraft.world.phys.Vec3
import java.util.function.Consumer


object RedstoneOutputScene {
    fun register(helper: PonderSceneRegistrationHelper<ResourceLocation>) {
        helper.forComponents(ModBlockRegistry.CONTRACT_PORTAL.id)
            .addStoryBoard(
                WingsContractsMod.prefix("contract_portal/redstone_output"),
                ::scene,
                ModPonderPlugin.CONTRACT_CATEGORY
            )
    }

    fun scene(builder: SceneBuilder, util: SceneBuildingUtil) {
        val scene = CreateSceneBuilder(builder)
        scene.title("contract_portal.redstone_output", "title")
        scene.configureBasePlate(0, 0, 5)
        scene.showBasePlate()
        scene.idle(10)
        scene.world().showSection(util.select().fromTo(0, 1, 0, 4, 1, 4), Direction.DOWN)

        val portalPosition = util.grid().at(1, 1, 2)
        val comparatorPosition = util.grid().at(2, 1, 2)
        val nixieTubePosition = util.grid().at(3, 1, 2)

        val contract = ModPonderPlugin.getExampleContract(scene.scene.world)
        scene.overlay()
            .showControls(util.vector().topOf(portalPosition), Pointing.DOWN, 40)
            .rightClick()
            .withItem(contract)
        scene.world().modifyBlockEntity<ContractPortalBlockEntity>(
            portalPosition, ContractPortalBlockEntity::class.java
        ) {
            it.contractSlot = contract
        }
        scene.world().modifyBlock(
            portalPosition,
            { it.setValue(ContractPortalBlock.MODE, ContractPortalMode.LIT) },
            false
        )

        scene.idle(75)

        val droppedItems = listOf(
            ItemStack(Items.DIAMOND, 2),
            ItemStack(Items.DIAMOND, 2),
            ItemStack(Items.DIAMOND, 2),
            ItemStack(Items.DIAMOND, 2),
        )

        droppedItems.forEach {
            var item = scene.world().createItemEntity(portalPosition.above(2).center, Vec3.ZERO, it)
            scene.idle(10)
            scene.world().modifyEntity(item, Entity::discard)
        }

        scene.world().modifyBlock(
            comparatorPosition,
            { it.setValue(ComparatorBlock.POWERED, true) },
            false
        )
        scene.world().modifyBlockEntityNBT(
            util.select().position(nixieTubePosition),
            NixieTubeBlockEntity::class.java,
            Consumer { it.putInt("RedstoneStrength", 1) })

        scene.idle(50)

        scene.overlay()
            .showText(75)
            .text("text1")
            .attachKeyFrame()
            .pointAt(util.vector().topOf(portalPosition))
            .placeNearTarget()

        scene.idle(75)
    }
}