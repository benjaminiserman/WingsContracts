package dev.biserman.wingscontracts.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.data.AvailableContractsManager
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import kotlin.math.min

object DebugContractCommand {
    fun register(): ArgumentBuilder<CommandSourceStack, *> =
        Commands.literal("debug")
            .requires { context -> context.entity is Player }
            .then(
                Commands.literal("list").then(
                    Commands.argument("page", IntegerArgumentType.integer())
                        .executes { context ->
                            listPage(
                                context.source,
                                IntegerArgumentType.getInteger(context, "page")
                            )
                        })
            )

    const val PAGE_SIZE = 20
    fun listPage(sourceStack: CommandSourceStack, page: Int): Int {
        val maxPage = (AvailableContractsManager.availableContracts.size / PAGE_SIZE) + 1
        if (page > maxPage) {
            sourceStack.sendFailure(
                Component.translatable(
                    "commands.${WingsContractsMod.MOD_ID}.failed.unavailable_page", page,
                    AvailableContractsManager.availableContracts.size / PAGE_SIZE
                )
            )
            return 0
        }

        sourceStack.sendSuccess({ Component.literal("==========================") }, true)
        for (entry in AvailableContractsManager.availableContracts.withIndex().toList().subList(
            (page - 1) * PAGE_SIZE,
            min(page * PAGE_SIZE, AvailableContractsManager.availableContracts.size)
        )) {
            sourceStack.sendSuccess({ Component.literal("${entry.index}. ${entry.value.tag}") }, true)
        }
        sourceStack.sendSuccess({ Component.literal("$page/$maxPage") }, true)

        return 1
    }
}