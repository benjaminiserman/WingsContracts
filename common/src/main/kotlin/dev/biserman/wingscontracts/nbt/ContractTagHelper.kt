@file:Suppress("MoveLambdaOutsideParentheses")

package dev.biserman.wingscontracts.nbt

import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import dev.biserman.wingscontracts.config.ModConfig
import net.minecraft.client.Minecraft
import net.minecraft.core.HolderLookup
import net.minecraft.core.RegistryAccess
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import kotlin.math.max
import kotlin.reflect.KProperty

@JvmInline
value class ContractTag(val tag: CompoundTag) {
    override fun toString() = "ContractTag($tag)"

    companion object {
        fun fromJson(json: JsonObject): ContractTag =
            ContractTag(JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json) as CompoundTag)
    }
}

sealed class Reward {
    class Defined(val itemStack: ItemStack) : Reward()
    class Random(val value: Double) : Reward()
}

@Suppress("MemberVisibilityCanBePrivate")
object ContractTagHelper {
    const val CONTRACT_INFO_KEY = "contractInfo"
    var registryAccess: HolderLookup.Provider? = null

    fun getContractTag(contractItemStack: ItemStack): ContractTag? {
        val contractTag =
            contractItemStack.components.get(DataComponents.CUSTOM_DATA)?.tag?.getCompound(CONTRACT_INFO_KEY)
                ?: return null
        return ContractTag(contractTag)
    }

    fun setContractTag(contractItemStack: ItemStack, contractTag: ContractTag) {
        contractItemStack.update(DataComponents.CUSTOM_DATA, CustomData.of(CompoundTag())) {
            // unsafe mutations!
            it.tag.put(
                CONTRACT_INFO_KEY,
                contractTag.tag
            )
            it
        }
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

    fun reward(key: String? = null) =
        Property(key, safeGet {
            if (this.contains(it, 99)) { // if the tag is just an integer, replace with default reward
                val loadedValue = max(1.0, this.getDouble(it))
                return@safeGet Reward.Random(loadedValue)
            } else if (this.contains(it)) {
                val itemStack = ItemStack.parse(
                    registryAccess
                        ?: return@safeGet Reward.Defined(ItemStack.EMPTY),
                    this.getCompound(it)
                ).orElse(ItemStack.EMPTY)
                itemStack.count = this.getCompound(it).getInt("Count")
                return@safeGet Reward.Defined(itemStack)
            } else {
                return@safeGet Reward.Random(ModConfig.SERVER.defaultRewardMultiplier.get())
            }
        }, safeWrite@{ safeKey, value ->
            when (value) {
                is Reward.Defined -> {
                    val tag = value.itemStack.save(
                        registryAccess
                            ?: return@safeWrite,
                        CompoundTag()
                    ) as CompoundTag
                    tag.putInt("Count", value.itemStack.count)
                    this.put(safeKey, tag)
                }
                is Reward.Random -> this.putDouble(safeKey, value.value)
            }
        })

    fun itemStack(key: String? = null) =
        Property(key, safeGet {
            if (this.contains(it)) {
                val itemStack = ItemStack.parse(
                    registryAccess
                        ?: return@safeGet ItemStack.EMPTY,
                    this.getCompound(it)
                ).orElse(ItemStack.EMPTY)
                itemStack.count = this.getCompound(it).getInt("Count")
                return@safeGet itemStack
            } else {
                return@safeGet null
            }
        }, safeWrite@{ safeKey, value ->
            val tag = value.save(
                registryAccess
                    ?: return@safeWrite,
                CompoundTag()
            ) as CompoundTag
            tag.putInt("Count", value.count)
            this.put(safeKey, tag)
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