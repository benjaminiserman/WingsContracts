package dev.biserman.wingscontracts.tag

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import kotlin.reflect.KProperty

@JvmInline
value class ContractTag(val tag: CompoundTag)

@Suppress("MemberVisibilityCanBePrivate", "MoveLambdaOutsideParentheses")
object ContractTagHelper {
    const val CONTRACT_INFO_KEY = "contractInfo"

    fun getContractTag(contractItemStack: ItemStack): ContractTag? {
        val contractTag = contractItemStack.tag?.getCompound(CONTRACT_INFO_KEY) ?: return null
        return ContractTag(contractTag)
    }

    fun setContractTag(contractItemStack: ItemStack, contractTag: ContractTag) {
        contractItemStack.addTagElement(CONTRACT_INFO_KEY, contractTag.tag)
    }

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
    fun uuid(key: String? = null) = Property(key, safeGet(CompoundTag::getUUID), CompoundTag::putUUID)
    fun float(key: String? = null) = Property(key, safeGet(CompoundTag::getFloat), CompoundTag::putFloat)
    fun double(key: String? = null) = Property(key, safeGet(CompoundTag::getDouble), CompoundTag::putDouble)
    fun boolean(key: String? = null) = Property(key, safeGet(CompoundTag::getBoolean), CompoundTag::putBoolean)
    fun itemStack(key: String? = null) =
        Property(key, safeGet { ItemStack.of(this.getCompound(it)) }, { safeKey, value ->
            this.put(safeKey, value.save(CompoundTag()))
        })

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