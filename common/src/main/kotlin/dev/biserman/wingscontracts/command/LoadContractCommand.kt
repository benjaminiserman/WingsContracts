package dev.biserman.wingscontracts.command

import com.google.gson.JsonParser
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.command.ModCommand.giveContract
import dev.biserman.wingscontracts.core.AbyssalContract
import dev.biserman.wingscontracts.core.Contract
import dev.biserman.wingscontracts.data.AvailableContractsManager
import dev.biserman.wingscontracts.server.AvailableContractsData
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.world.level.Level

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
                                    loadContract(
                                        StringArgumentType.getString(context, "jsonString"),
                                        context.source.level
                                    ),
                                    EntityArgument.getPlayers(context, "targets")
                                )
                            }).executes { context ->
                        giveContract(
                            context.source,
                            loadContract(StringArgumentType.getString(context, "jsonString"), context.source.level),
                            EntityArgument.getPlayers(context, "targets")
                        )
                    })

    fun loadContract(jsonString: String, level: Level): Contract {
        try {
            val index = jsonString.toIntOrNull()
            if (index != null) {
                return AvailableContractsData.get(level)
                    .generateContract(AvailableContractsManager.availableContracts[index])
            }

            val tag =
                AbyssalContract.fromJson(JsonParser.parseString(jsonString).asJsonObject) // add support for other types later
            return AbyssalContract.load(tag, AvailableContractsData.get(level))
        } catch (e: Error) {
            WingsContractsMod.LOGGER.error(e)
            throw e
        }
    }
}