package dev.biserman.wingscontracts.command

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.api.AbyssalContract
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.item.ItemArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import java.util.*

object ContractCommand {
    fun register(commandBuildContext: CommandBuildContext): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("contract")
            .requires { cs: CommandSourceStack -> cs.hasPermission(2) }
            .then(
                Commands.argument("targetItem", ItemArgument.item(commandBuildContext))
                    .then(
                        Commands.argument("countPerUnit", IntegerArgumentType.integer())
                            .then(
                                Commands.argument("rewardItem", ItemArgument.item(commandBuildContext))
                                    .then(
                                        Commands.argument("unitPrice", IntegerArgumentType.integer())
                                            .then(
                                                Commands.argument("baseUnitsDemanded", IntegerArgumentType.integer())
                                                    .then(
                                                        Commands.argument(
                                                            "quantityGrowthFactor",
                                                            DoubleArgumentType.doubleArg()
                                                        )
                                                            .then(
                                                                Commands.argument(
                                                                    "maxLevel",
                                                                    IntegerArgumentType.integer()
                                                                )
                                                                    .executes { context: CommandContext<CommandSourceStack> ->
                                                                        val targetItem = ItemArgument.getItem(
                                                                            context,
                                                                            "targetItem"
                                                                        ).item
                                                                        val rewardItem = ItemArgument.getItem(
                                                                            context,
                                                                            "rewardItem"
                                                                        ).item

                                                                        val unitPrice = IntegerArgumentType
                                                                            .getInteger(context, "unitPrice")
                                                                        val countPerUnit = IntegerArgumentType
                                                                            .getInteger(context, "countPerUnit")
                                                                        val baseUnitsDemanded = IntegerArgumentType
                                                                            .getInteger(context, "baseUnitsDemanded")
                                                                        val quantityGrowthFactor = DoubleArgumentType
                                                                            .getDouble(context, "quantityGrowthFactor")
                                                                        val maxLevel = IntegerArgumentType
                                                                            .getInteger(context, "maxLevel")

                                                                        if (unitPrice <= 0) {
                                                                            context.source
                                                                                .sendFailure(Component.translatable("${WingsContractsMod.MOD_ID}.command.contract.unit_price_greater_than_zero"))
                                                                        }

                                                                        if (countPerUnit <= 0) {
                                                                            context.source
                                                                                .sendFailure(Component.translatable("${WingsContractsMod.MOD_ID}.command.contract.count_per_unit_greater_than_zero"))
                                                                        }

                                                                        if (baseUnitsDemanded <= 0) {
                                                                            context.source
                                                                                .sendFailure(Component.translatable("${WingsContractsMod.MOD_ID}.command.contract.base_units_demanded_greater_than_zero"))
                                                                        }

                                                                        if (quantityGrowthFactor < 0) {
                                                                            context.source
                                                                                .sendFailure(Component.translatable("${WingsContractsMod.MOD_ID}.command.contract.quantity_growth_non_negative"))
                                                                        }

                                                                        if (maxLevel <= 0 && maxLevel != -1) {
                                                                            context.source
                                                                                .sendFailure(Component.translatable("${WingsContractsMod.MOD_ID}.command.contract.max_level_negative_one_or_positive"))
                                                                        }

                                                                        val author =
                                                                            Objects.requireNonNull(context.source.player)
                                                                                ?.name
                                                                                ?.string
                                                                                ?: Component.translatable("${WingsContractsMod.MOD_ID}.command.contract.unknown_author").string

                                                                        val contract = AbyssalContract(
                                                                            id = UUID.randomUUID(),
                                                                            targetItems = listOf(targetItem),
                                                                            targetTags = listOf(),
                                                                            startTime = System.currentTimeMillis(),
                                                                            currentCycleStart = System.currentTimeMillis(),
                                                                            cycleDurationMs = 1000L * 60 * 5,
                                                                            countPerUnit = countPerUnit,
                                                                            baseUnitsDemanded = baseUnitsDemanded,
                                                                            unitsFulfilled = 0,
                                                                            unitsFulfilledEver = 0,
                                                                            isActive = true,
                                                                            isLoaded = false,
                                                                            author = author,
                                                                            name = null,
                                                                            reward = ItemStack(rewardItem, unitPrice),
                                                                            level = 1,
                                                                            quantityGrowthFactor = quantityGrowthFactor,
                                                                            maxLevel = maxLevel,
                                                                        ).createItem()

                                                                        giveContract(
                                                                            context.source, contract,
                                                                            context.source.player!!
                                                                        )
                                                                    })
                                                    )
                                            )
                                    )
                            )
                    )
            )
    }

    private fun giveContract(
        commandSourceStack: CommandSourceStack,
        contract: ItemStack,
        serverPlayer: ServerPlayer
    ): Int {
        val didAdd = serverPlayer.inventory.add(contract)
        if (didAdd && contract.isEmpty) {
            contract.count = 1
            val itemEntity = serverPlayer.drop(contract, false)
            itemEntity?.makeFakeItem()

            serverPlayer.level().playSound(
                null, serverPlayer.x, serverPlayer.y, serverPlayer.z,
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f,
                ((serverPlayer.random.nextFloat() - serverPlayer.random.nextFloat()) * 0.7f + 1.0f)
                        * 2.0f
            )
            serverPlayer.containerMenu.broadcastChanges()
        } else {
            val itemEntity = serverPlayer.drop(contract, false)
            if (itemEntity != null) {
                itemEntity.setNoPickUpDelay()
                itemEntity.setTarget(serverPlayer.uuid)
            }
        }

        commandSourceStack.sendSuccess({
            Component.translatable(
                "commands.give.success.single",
                1, contract.displayName, serverPlayer.displayName
            )
        }, true)

        return 1
    }
}
