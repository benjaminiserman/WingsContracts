package dev.biserman.wingscontracts.entity

import com.simibubi.create.infrastructure.ponder.scenes.fluid.HosePulleyScenes.level
import dev.biserman.wingscontracts.nbt.ContractTagHelper.itemStack
import dev.biserman.wingscontracts.registry.ModEntityRegistry
import net.minecraft.core.SectionPos.z
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class FakeItemEntity(entityType: EntityType<FakeItemEntity>, level: Level) :
    ItemEntity(entityType, level) {

    constructor (
        level: Level,
        x: Double,
        y: Double,
        z: Double,
        itemStack: ItemStack
    ) : this(ModEntityRegistry.FAKE_ITEM.get(), level) {
        setPos(x, y, z)
        this.item = itemStack
    }

    val realItemEntity: ItemEntity = ItemEntity(this)

    init {
        setNeverPickUp()
        realItemEntity.setNeverPickUp()
    }

    override fun getItem(): ItemStack {
        return ItemStack.EMPTY
    }

    override fun tick() {
        super.tick()
        realItemEntity.tick()

        realItemEntity.setPos(x, y, z)
        realItemEntity.deltaMovement = deltaMovement
        realItemEntity.bobOffs = bobOffs
    }
}