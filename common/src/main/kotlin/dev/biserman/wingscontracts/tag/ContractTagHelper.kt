package dev.biserman.wingscontracts.tag

import net.minecraft.nbt.CompoundTag
import kotlin.reflect.KProperty

@JvmInline
value class ContractTag(val tag: CompoundTag)

@Suppress("MemberVisibilityCanBePrivate")
object ContractTagHelper {
    const val CONTRACT_ID = "contractID"

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
}