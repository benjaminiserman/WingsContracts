package dev.biserman.wingscontracts.command

import com.mojang.brigadier.builder.ArgumentBuilder
import dev.biserman.wingscontracts.command.ModCommand.giveContract
import dev.biserman.wingscontracts.data.ContractSavedData
import dev.biserman.wingscontracts.data.AvailableContractsManager
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument

object RandomContractCommand {
    fun register(): ArgumentBuilder<CommandSourceStack, *> =
        Commands.literal("random")
            .then(
                Commands.argument("targets", EntityArgument.players())
                    .executes { context ->
                        giveContract(
                            context.source,
                            ContractSavedData
                                .get(context.source.level)
                                .generator
                                .generateContract(AvailableContractsManager.randomTag()),
                            EntityArgument.getPlayers(context, "targets")
                        )
                    })
}