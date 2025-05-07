package dev.biserman.wingscontracts.advancements

import com.google.gson.JsonObject
import dev.biserman.wingscontracts.WingsContractsMod.prefix
import dev.biserman.wingscontracts.nbt.ItemConditionParser
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.advancements.critereon.*
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import java.util.function.Predicate

class ContractCompleteTrigger : SimpleCriterionTrigger<ContractCompleteTrigger.Instance>() {
    override fun getId() = ID

    public override fun createInstance(
        json: JsonObject, playerPred: ContextAwarePredicate, conditions: DeserializationContext
    ) = Instance(
        playerPred, json.get("item_matches").toString(), LocationPredicate.fromJson(json.get("location"))
    )


    fun trigger(player: ServerPlayer, stack: ItemStack, world: ServerLevel, x: Int, y: Int, z: Int) {
        trigger(player, Predicate { instance: Instance -> instance.test(stack, world, x, y, z) })
    }

    class Instance(
        playerPredicate: ContextAwarePredicate, val conditionString: String, val location: LocationPredicate
    ) : AbstractCriterionTriggerInstance(
        ID, playerPredicate
    ) {
        val conditions by lazy { ItemConditionParser.parse(conditionString) }
        override fun getCriterion() = ID

        fun test(stack: ItemStack, world: ServerLevel, x: Int, y: Int, z: Int): Boolean {
            return conditions.all { it.match(stack) } && this.location.matches(
                world, x.toDouble(), y.toDouble(), z.toDouble()
            )
        }

        override fun serializeToJson(serializationContext: SerializationContext): JsonObject {
            val json: JsonObject = super.serializeToJson(serializationContext)
            if (conditions !== NbtPredicate.ANY) {
                json.addProperty("item_matches", conditionString)
            }
            if (location !== LocationPredicate.ANY) {
                json.add("location", location.serializeToJson())
            }

            return json
        }
    }

    companion object {
        val ID = prefix("contract_complete")
        val INSTANCE = CriteriaTriggers.register(ContractCompleteTrigger())
    }
}