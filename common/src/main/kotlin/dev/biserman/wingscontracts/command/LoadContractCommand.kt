package dev.biserman.wingscontracts.command

import com.google.gson.JsonParser
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.api.AbyssalContract
import dev.biserman.wingscontracts.api.Contract
import dev.biserman.wingscontracts.command.ModCommand.giveContract
import dev.biserman.wingscontracts.data.LoadedContracts
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument

object LoadContractCommand {
    fun register(): ArgumentBuilder<CommandSourceStack, *> =
        Commands.literal("load")
            .then(
                Commands.argument("jsonString", StringArgumentType.string())
                    .then(
                        Commands.argument("targets", EntityArgument.players())
                            .executes { context ->
                                giveContract(
                                    context.source,
                                    loadContract(StringArgumentType.getString(context, "jsonString")),
                                    EntityArgument.getPlayers(context, "targets")
                                )
                            }).executes { context ->
                        giveContract(
                            context.source,
                            loadContract(StringArgumentType.getString(context, "jsonString")),
                            EntityArgument.getPlayers(context, "targets")
                        )
                    })

    fun loadContract(jsonString: String): Contract? {
        try {
            val tag =
                AbyssalContract.fromJson(JsonParser.parseString(jsonString).asJsonObject) // add support for other types later
            return LoadedContracts[tag]
        } catch (e: Error) {
            WingsContractsMod.LOGGER.error(e)
            throw e
        }
    }
}