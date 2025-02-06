package dev.biserman.wingscontracts.tag

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

@Suppress("MoveLambdaOutsideParentheses")
class ContractTag(val tag: CompoundTag) {
    // if this is too bad for GC let's do this instead ↓
    //    var contractInfo
    //        get() = wrapGet { getString("contractInfo") }()
    //        set(value) { putString("contractInfo", value ?: "") }

    val targetItemKey by lazy { string("targetItem") }
    val targetTagKey by lazy { string("targetTag") }
    val rewardItemKey by lazy { string("rewardItem").withDefault("minecraft:emerald") }
    val unitPrice by lazy { int("unitPrice").withDefault(1) }
    val countPerUnit by lazy { int("countPerUnit").withDefault(64) }
    val levelOneQuantity by lazy { int("levelOneQuantity").withDefault(256) }
    val quantityGrowthFactor by lazy { float("quantityGrowthFactor").withDefault(0.5f) }
    val startLevel by lazy { int("startLevel").withDefault(1) }
    val level by lazy { int("level").withDefault(1) }
    val startTime by lazy { long("startTime") }
    val currentCycleStart by lazy { long("currentCycleStart").withDefault(startTime.get()) }
    val cycleDurationMs by lazy { long("cycleDurationMs").withDefault(1000L * 60 * 60 * 24 * 7) }
    val quantityFulfilled by lazy { int("quantityFulfilled").withDefault(0) }
    val quantityFulfilledEver by lazy { long("quantityFulfilledEver").withDefault(0) }
    val maxLevel by lazy { int("maxLevel").withDefault(10) }
    val author by lazy { string("author") }

    class Property<T>(val get: () -> T?, val put: (T?) -> Unit) {
        fun withDefault(default: T & Any) = NonNullProperty({ get() ?: default }, { put(it) })
        fun withDefault(default: T?) = Property({ get() ?: default }, { put(it ?: default) })
    }

    class NonNullProperty<T : Any>(val get: () -> T, val put: (T) -> Unit)

    private inline fun <T> wrapGet(crossinline fn: () -> T): () -> T? {
        return inner@{
            try {
                return@inner fn()
            } catch (e: Exception) {
                return@inner null
            }
        }
    }

    private fun string(key: String): Property<String> =
        Property(wrapGet { tag.getString(key) }, { putValue -> tag.putString(key, putValue ?: "") })

    private fun int(key: String): Property<Int> =
        Property(wrapGet { tag.getInt(key) }, { putValue -> tag.putInt(key, putValue ?: 0) })

    private fun long(key: String): Property<Long> =
        Property(wrapGet { tag.getLong(key) }, { putValue -> tag.putLong(key, putValue ?: 0L) })

    private fun float(key: String): Property<Float> =
        Property(wrapGet { tag.getFloat(key) }, { putValue -> tag.putFloat(key, putValue ?: 0f) })

    val quantityDemanded
        get() = getQuantityDemanded(
            this.levelOneQuantity.get(),
            this.level.get(),
            this.quantityGrowthFactor.get(),
            this.countPerUnit.get()
        )

    val targetTag: TagKey<Item>?
        get() {
            val tagKey = targetTagKey.get() ?: return null
            if (tagKey.isNotEmpty()) {
                return TagKey.create(Registries.ITEM, ResourceLocation.tryParse(tagKey) ?: return null)

            }

            return null
        }

    val targetItem: Item?
        get() {
            val targetItemKey = targetItemKey.get() ?: return null
            return BuiltInRegistries.ITEM[ResourceLocation.tryParse(
                targetItemKey
            )]
        }

    val rewardItem: Item?
        get() {
            val rewardItemKey = rewardItemKey.get() ?: return null
            return BuiltInRegistries.ITEM[ResourceLocation.tryParse(
                rewardItemKey
            )]
        }

    companion object {
        const val CONTRACT_INFO = "contractInfo"
        fun getQuantityDemanded(
            levelOneQuantity: Int, level: Int, quantityGrowthFactor: Float,
            countPerUnit: Int
        ): Int {
            val quantity = levelOneQuantity + (levelOneQuantity * (level - 1) * quantityGrowthFactor).toInt()
            return quantity - quantity % countPerUnit
        }
    }
}