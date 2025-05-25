package dev.biserman.wingscontracts.entity

import dev.biserman.wingscontracts.registry.ModEntityRegistry
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
        itemStack: ItemStack,
    ) : this(ModEntityRegistry.FAKE_ITEM.get(), level) {
        setPos(x, y, z)
        setDeltaMovement(0.0, 0.0, 0.0)
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
    }
}