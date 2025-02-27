@file:Suppress("MoveLambdaOutsideParentheses")

package dev.biserman.wingscontracts.tag

import dev.biserman.wingscontracts.config.ModConfig
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack
import kotlin.reflect.KProperty

@JvmInline
value class ContractTag(val tag: CompoundTag)

@Suppress("MemberVisibilityCanBePrivate")
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
        val key: String?,
        val getFn: (CompoundTag).(String) -> T?,
        val putFn: (CompoundTag).(String, T) -> Unit,
    ) {
        operator fun getValue(ref: ContractTag, prop: KProperty<*>): T? {
            if (!ref.tag.contains(key ?: prop.name)) {
                return null
            }

            return ref.tag.getFn(key ?: prop.name)
        }

        operator fun setValue(ref: ContractTag, prop: KProperty<*>, value: T?) {
            ref.tag.putFn(key ?: prop.name, value ?: return)
        }
    }

    fun string(key: String? = null) =
        Property(key, safeGet(CompoundTag::getString), CompoundTag::putString)

    fun int(key: String? = null) =
        Property(key, safeGet(CompoundTag::getInt), CompoundTag::putInt)

    fun long(key: String? = null) =
        Property(key, safeGet(CompoundTag::getLong), CompoundTag::putLong)

    fun uuid(key: String? = null) = Property(key, safeGet(CompoundTag::getUUID), CompoundTag::putUUID)
    fun float(key: String? = null) =
        Property(key, safeGet(CompoundTag::getFloat), CompoundTag::putFloat)

    fun double(key: String? = null) =
        Property(key, safeGet(CompoundTag::getDouble), CompoundTag::putDouble)

    fun boolean(key: String? = null) = Property(
        key, safeGet(CompoundTag::getBoolean), CompoundTag::putBoolean
    )

    fun itemStack(key: String? = null) =
        Property(key, safeGet {
            if (this.contains(it, 99)) {
                return@safeGet ItemStack(
                    ModConfig.SERVER.defaultRewardCurrency,
                    Mth.ceil(this.getInt(it).toDouble() * ModConfig.SERVER.defaultRewardCurrencyMultiplier.get())
                )
            } else if (this.contains(it)) {
                return@safeGet ItemStack.of(this.getCompound(it))
            } else {
                return@safeGet null
            }
        }, { safeKey, value ->
            this.put(safeKey, value.save(CompoundTag()))
        })

    fun (String).parseCsv() = this.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    fun csv(key: String? = null) =
        Property(key, safeGet { this.getString(it).parseCsv() }, { putKey, value ->
            this.putString(putKey, value.joinToString(","))
        })

    private inline fun <T> safeGet(crossinline fn: (CompoundTag).(String) -> T): (CompoundTag).(String) -> T? {
        return inner@{ key ->
            try {
                return@inner fn(key)
            } catch (_: Exception) {
                return@inner null
            }
        }
    }
}