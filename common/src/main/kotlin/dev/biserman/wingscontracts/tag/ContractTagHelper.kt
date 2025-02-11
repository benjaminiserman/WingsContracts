package dev.biserman.wingscontracts.tag

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

@Suppress("MoveLambdaOutsideParentheses", "MemberVisibilityCanBePrivate")
object ContractTagHelper {
    const val CONTRACT_INFO = "contractInfo"

    val targetItemKeys by lazy { csv("targetItems") }
    val targetTagKeys by lazy { csv("targetTags") }

    val startTime by lazy { long("startTime") }
    val currentCycleStart by lazy { long("currentCycleStart") }
    val cycleDurationMs by lazy { long("cycleDurationMs") }

    val countPerUnit by lazy { int("countPerUnit") }
    val baseUnitsDemanded by lazy { int("baseUnitsDemanded") }
    val unitsFulfilled by lazy { int("unitsFulfilled") }
    val unitsFulfilledEver by lazy { long("unitsFulfilledEver") }

    val isActive by lazy { boolean("isActive") }
    val author by lazy { string("author") }

    val rewardItemKey by lazy { string("rewardItem") }
    val unitPrice by lazy { int("unitPrice") }

    val level by lazy { int("level") }
    val quantityGrowthFactor by lazy { float("quantityGrowthFactor") }
    val maxLevel by lazy { int("maxLevel") }

    class Property<T>(val get: (CompoundTag) -> T?, val put: (CompoundTag, T?) -> Unit)

    private inline fun <T> wrapGet(crossinline fn: (CompoundTag) -> T): (CompoundTag) -> T? {
        return inner@{ tag ->
            try {
                return@inner fn(tag)
            } catch (e: Exception) {
                return@inner null
            }
        }
    }

    private fun csv(key: String): Property<List<String>> =
        Property(
            wrapGet { tag -> tag.getString(key).split(",").map { it.trim() }.filter { it.isNotEmpty() } },
            { tag, value -> tag.putString(key, value?.joinToString(",") ?: return@Property) })

    private fun string(key: String): Property<String> =
        Property(wrapGet { tag -> tag.getString(key) }, { tag, value -> tag.putString(key, value ?: return@Property) })

    private fun int(key: String): Property<Int> =
        Property(wrapGet { tag -> tag.getInt(key) }, { tag, value -> tag.putInt(key, value ?: return@Property) })

    private fun long(key: String): Property<Long> =
        Property(wrapGet { tag -> tag.getLong(key) }, { tag, value -> tag.putLong(key, value ?: return@Property) })

    private fun float(key: String): Property<Float> =
        Property(wrapGet { tag -> tag.getFloat(key) }, { tag, value -> tag.putFloat(key, value ?: return@Property) })

    private fun double(key: String): Property<Double> =
        Property(wrapGet { tag -> tag.getDouble(key) }, { tag, value -> tag.putDouble(key, value ?: return@Property) })

    private fun boolean(key: String): Property<Boolean> =
        Property(
            wrapGet { tag -> tag.getBoolean(key) },
            { tag, value -> tag.putBoolean(key, value ?: return@Property) })

    val targetTags by lazy {
        Property<List<TagKey<Item>>?>(
            { tag ->
                val tagKeys = targetTagKeys.get(tag) ?: return@Property null
                if (tagKeys.isNotEmpty()) {
                    return@Property tagKeys.map {
                        TagKey.create(
                            Registries.ITEM,
                            ResourceLocation.tryParse(it) ?: ResourceLocation("")
                        )
                    }
                }

                return@Property null
            }, { _, _ -> throw NotImplementedError() })
    }

    val targetItems by lazy {
        Property<List<Item>?>(
            { tag ->
                val targetItems = targetItemKeys.get(tag) ?: return@Property null
                if (targetItems.isNotEmpty()) {
                    return@Property targetItems.map {
                        BuiltInRegistries.ITEM[ResourceLocation.tryParse(it)]
                    }
                }

                return@Property null
            }, { _, _ -> throw NotImplementedError() })
    }

    val rewardItem by lazy {
        Property<Item?>(
            { tag ->
                val rewardItem = rewardItemKey.get(tag) ?: return@Property null
                if (rewardItem.isNotEmpty()) {
                    return@Property BuiltInRegistries.ITEM[ResourceLocation.tryParse(rewardItem)]
                }

                return@Property null
            }, { _, _ -> throw NotImplementedError() })
    }
}