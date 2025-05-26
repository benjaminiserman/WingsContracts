package dev.biserman.wingscontracts.entity

import dev.biserman.wingscontracts.registry.ModEntityRegistry
import dev.biserman.wingscontracts.registry.ModItemRegistry
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class FakeItemEntity :
    ItemEntity {

    var realItem: ItemStack
        get() = this.getEntityData().get(DATA_REAL_ITEM)
        set(value) = this.getEntityData().set(DATA_REAL_ITEM, value)

    constructor (entityType: EntityType<FakeItemEntity>, level: Level) : super(entityType, level)

    constructor (
        level: Level,
        x: Double,
        y: Double,
        z: Double,
        itemStack: ItemStack,
    ) : this(ModEntityRegistry.FAKE_ITEM.get(), level) {
        setPos(x, y, z)
        setDeltaMovement(0.0, 0.0, 0.0)
        realItem = itemStack
    }

    init {
        item = ModItemRegistry.RED_EXCLAMATION_MARK.get().defaultInstance
    }

    override fun defineSynchedData() {
        super.defineSynchedData()
        this.getEntityData().define(DATA_REAL_ITEM, ItemStack.EMPTY)
    }

    override fun onSyncedDataUpdated(entityDataAccessor: EntityDataAccessor<*>) {
        super.onSyncedDataUpdated(entityDataAccessor)
        if (DATA_REAL_ITEM == entityDataAccessor) {
            this.realItem.entityRepresentation = this
        }
    }

    companion object {
        val DATA_REAL_ITEM: EntityDataAccessor<ItemStack> = SynchedEntityData.defineId(FakeItemEntity::class.java, EntityDataSerializers.ITEM_STACK);
    }
}