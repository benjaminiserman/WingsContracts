package dev.biserman.wingscontracts.command

import com.google.gson.JsonParser
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.command.ModCommand.giveContract
import dev.biserman.wingscontracts.core.AbyssalContract
import dev.biserman.wingscontracts.core.BoundContract
import dev.biserman.wingscontracts.core.Contract
import dev.biserman.wingscontracts.data.AvailableContractsManager
import dev.biserman.wingscontracts.nbt.ContractTag
import dev.biserman.wingscontracts.server.AvailableContractsData
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.world.level.Level

object LoadContractCommand {
    fun register(): ArgumentBuilder<CommandSourceStack, *> =
        Commands.literal("load")
            .then(
                Commands.argument("type", StringArgumentType.word())
                    .then(
                        Commands.argument("jsonString", StringArgumentType.string())
                            .then(
                                Commands.argument("targets", EntityArgument.players())
                                    .executes { context ->
                                        giveContract(
                                            context.source,
                                            loadContract(
                                                StringArgumentType.getString(context, "jsonString"),
                                                context.source.level,
                                                StringArgumentType.getString(context, "type"),
                                            ),
                                            EntityArgument.getPlayers(context, "targets")
                                        )
                                    }).executes { context ->
                                giveContract(
                                    context.source,
                                    loadContract(
                                        StringArgumentType.getString(context, "jsonString"),
                                        context.source.level,
                                        StringArgumentType.getString(context, "type"),
                                    ),
                                    EntityArgument.getPlayers(context, "targets")
                                )
                            })
            )

    fun loadContract(jsonString: String, level: Level, type: String): Contract {
        try {
            val index = jsonString.toIntOrNull()
            if (index != null) {
                return AvailableContractsData.get(level)
                    .generateContract(ContractTag(AvailableContractsManager.availableContracts[index].tag.copy()))
            }

            val tag =
                Contract.fromJson(JsonParser.parseString(jsonString).asJsonObject) // add support for other types later

            return when (type) {
                "abyssal" -> AbyssalContract.load(tag, AvailableContractsData.get(level))
                "bound" -> BoundContract.load(tag)
                else -> throw Error("Contract type not found: $type")
            }
        } catch (e: Error) {
            WingsContractsMod.LOGGER.error(e)
            throw e
        }
    }
}