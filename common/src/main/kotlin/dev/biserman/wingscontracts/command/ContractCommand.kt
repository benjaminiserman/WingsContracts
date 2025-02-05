package dev.biserman.wingscontracts.command

import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.biserman.wingscontracts.item.ContractItem.Companion.createContract
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.item.ItemArgument
import net.minecraft.core.registries.BuiltInRegistries
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
                .then(Commands.argument("targetItem", ItemArgument.item(commandBuildContext))
                .then(Commands.argument("countPerUnit",IntegerArgumentType.integer())
                .then(Commands.argument("rewardItem",ItemArgument.item(commandBuildContext))
                .then(Commands.argument("unitPrice", IntegerArgumentType.integer())
                .then(Commands.argument("levelOneQuantity", IntegerArgumentType.integer())
                .then(Commands.argument("quantityGrowthFactor", FloatArgumentType.floatArg())
                .then(Commands.argument("maxLevel", IntegerArgumentType.integer())
                    .executes { context: CommandContext<CommandSourceStack> ->
                        val targetItem = BuiltInRegistries.ITEM
                            .getKey(
                                ItemArgument
                                    .getItem(context, "targetItem")
                                    .item
                            )
                            .toString()
                        val rewardItem = BuiltInRegistries.ITEM
                            .getKey(
                                ItemArgument
                                    .getItem(context, "rewardItem")
                                    .item
                            )
                            .toString()

                        val unitPrice = IntegerArgumentType
                            .getInteger(context, "unitPrice")
                        val countPerUnit = IntegerArgumentType
                            .getInteger(context, "countPerUnit")
                        val levelOneQuantity = IntegerArgumentType
                            .getInteger(context, "levelOneQuantity")
                        val quantityGrowthFactor = FloatArgumentType
                            .getFloat(context, "quantityGrowthFactor")
                        val maxLevel = IntegerArgumentType
                            .getInteger(context, "maxLevel")

                        if (unitPrice <= 0) {
                            context.source
                                .sendFailure(Component.literal("unitPrice must be greater than 0"))
                        }

                        if (countPerUnit <= 0) {
                            context.source
                                .sendFailure(Component.literal("countPerUnit must be greater than 0"))
                        }

                        if (levelOneQuantity <= 0) {
                            context.source
                                .sendFailure(Component.literal("levelOneQuantity must be greater than 0"))
                        }

                        if (quantityGrowthFactor < 0) {
                            context.source
                                .sendFailure(Component.literal("quantityGrowthFactor must be non-negative"))
                        }

                        if (maxLevel <= 0 && maxLevel != -1) {
                            context.source
                                .sendFailure(Component.literal("maxLevel must be -1 or greater than 0"))
                        }

                        val contract = createContract(
                            targetItem,
                            null,
                            rewardItem,
                            unitPrice,
                            countPerUnit,
                            levelOneQuantity,
                            quantityGrowthFactor,
                            1,
                            maxLevel,
                            Objects.requireNonNull(context.source.player)
                                ?.name
                                ?.string
                        )
                        giveContract(
                            context.source, contract,
                            context.source.player!!
                        )
                    })
                ))))))
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
