package dev.biserman.wingscontracts.tag

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import kotlin.reflect.KProperty

@JvmInline
value class ContractTag(val tag: CompoundTag) {
    companion object {
        fun from(stack: ItemStack): ContractTag? {
            return ContractTag(stack.tag?.getCompound(ContractTagHelper.CONTRACT_INFO) ?: return null)
        }

        fun from(tag: CompoundTag): ContractTag? {
            return ContractTag(tag.getCompound(ContractTagHelper.CONTRACT_INFO) ?: return null)
        }
    }
}

@Suppress("MemberVisibilityCanBePrivate")
object ContractTagHelper {
    const val CONTRACT_INFO = "contractInfo"

    // Shared Properties
    var (ContractTag).targetItemKeys by csv("targetItems")
    var (ContractTag).targetTagKeys by csv("targetTags")

    var (ContractTag).startTime by long()
    var (ContractTag).currentCycleStart by long()
    var (ContractTag).cycleDurationMs by long()

    var (ContractTag).countPerUnit by int()
    var (ContractTag).baseUnitsDemanded by int()
    var (ContractTag).unitsFulfilled by int()
    var (ContractTag).unitsFulfilledEver by long()

    var (ContractTag).isActive by boolean()
    var (ContractTag).author by string()

    // Abyssal Contract Properties
    var (ContractTag).rewardItemKey by string("rewardItem")
    var (ContractTag).unitPrice by int()

    var (ContractTag).level by int()
    var (ContractTag).quantityGrowthFactor by double()
    var (ContractTag).maxLevel by int()

    class Property<T>(
        val key: String?, val getFn: (CompoundTag).(String) -> T?, val putFn: (CompoundTag).(String, T) -> Unit
    ) {
        operator fun getValue(ref: ContractTag, prop: KProperty<*>): T? = ref.tag.getFn(key ?: prop.name)
        operator fun setValue(ref: ContractTag, prop: KProperty<*>, value: T?) {
            ref.tag.putFn(key ?: prop.name, value ?: return)
        }
    }

    fun string(key: String? = null) = Property(key, safeGet(CompoundTag::getString), CompoundTag::putString)
    fun int(key: String? = null) = Property(key, safeGet(CompoundTag::getInt), CompoundTag::putInt)
    fun long(key: String? = null) = Property(key, safeGet(CompoundTag::getLong), CompoundTag::putLong)
    fun float(key: String? = null) = Property(key, safeGet(CompoundTag::getFloat), CompoundTag::putFloat)
    fun double(key: String? = null) = Property(key, safeGet(CompoundTag::getDouble), CompoundTag::putDouble)
    fun boolean(key: String? = null) = Property(key, safeGet(CompoundTag::getBoolean), CompoundTag::putBoolean)

    @Suppress("MoveLambdaOutsideParentheses")
    fun csv(key: String? = null) = Property(
        key,
        safeGet { getKey -> this.getString(getKey).split(",").map { it.trim() }.filter { it.isNotEmpty() } },
        { putKey, value ->
            this.putString(putKey, value.joinToString(","))
        })

    private inline fun <T> safeGet(crossinline fn: (CompoundTag).(String) -> T): (CompoundTag).(String) -> T? {
        return inner@{ key ->
            try {
                return@inner fn(key)
            } catch (e: Exception) {
                return@inner null
            }
        }
    }

    var (ContractTag).targetTags: List<TagKey<Item>>?
        get() {
            val tagKeys = targetTagKeys ?: return null
            if (tagKeys.isNotEmpty()) {
                return tagKeys.map {
                    TagKey.create(
                        Registries.ITEM, ResourceLocation.tryParse(it) ?: ResourceLocation("")
                    )
                }
            }

            return null
        }
        set(value) {
            targetTagKeys = value?.map { it.registry.location().toString() }
        }

    var (ContractTag).targetItems: List<Item>?
        get() {
            val targetItems = targetItemKeys ?: return null
            if (targetItems.isNotEmpty()) {
                return targetItems.map {
                    BuiltInRegistries.ITEM[ResourceLocation.tryParse(it)]
                }
            }

            return null
        }
        set(value) {
            targetItems = value?.map { it.  .registry.location().toString() }
        }

    val (ContractTag).rewardItem: Item?
        get() {
            val rewardItem = rewardItemKey ?: return null
            if (rewardItem.isNotEmpty()) {
                return BuiltInRegistries.ITEM[ResourceLocation.tryParse(rewardItem)]
            }

            return null
        }
}