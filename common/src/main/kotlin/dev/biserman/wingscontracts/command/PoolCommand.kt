package dev.biserman.wingscontracts.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.data.ContractSavedData
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component

object PoolCommand {
    fun register(): ArgumentBuilder<CommandSourceStack, *> =
        Commands.literal("pool")
            .then(Commands.literal("refresh").executes { context ->
                context.source.sendSuccess(
                    { Component.translatable("commands.${WingsContractsMod.MOD_ID}.refresh") },
                    true
                )
                ContractSavedData.get(context.source.level).refresh(context.source.level)
                0
            })
            .then(Commands.literal("clear").executes { context ->
                context.source.sendSuccess(
                    { Component.translatable("commands.${WingsContractsMod.MOD_ID}.clear") },
                    true
                )
                ContractSavedData.get(context.source.level).clear(context.source.level)
                0
            })
            .then(
                Commands.literal("picks")
                    .then(
                        Commands.literal("set")
                            .then(
                                Commands.argument("target", EntityArgument.players())
                                    .then(
                                        Commands.argument("amount", IntegerArgumentType.integer()).executes { context ->
                                            val player = EntityArgument.getPlayer(context, "target")
                                            val amount = IntegerArgumentType.getInteger(context, "amount")
                                            ContractSavedData.setRemainingPicks(player, amount, true)
                                            context.source.sendSuccess({
                                                Component.translatable(
                                                    "commands.${WingsContractsMod.MOD_ID}.set_picks",
                                                    player.name.string,
                                                    amount
                                                )
                                            }, true)

                                            0
                                        })
                            )
                    ).then(
                        Commands.literal("add").then(
                            Commands.argument("target", EntityArgument.players())
                                .then(
                                    Commands.argument(
                                        "amount",
                                        IntegerArgumentType.integer()
                                    ).executes { context ->
                                        val player = EntityArgument.getPlayer(context, "target")
                                        val amount = IntegerArgumentType.getInteger(context, "amount")
                                        ContractSavedData.addRemainingPicks(player, amount, true)
                                        context.source.sendSuccess({
                                            Component.translatable(
                                                "commands.${WingsContractsMod.MOD_ID}.add_picks",
                                                player.name.string,
                                                amount
                                            )
                                        }, true)

                                        0
                                    })
                        )
                    )
            )

}
