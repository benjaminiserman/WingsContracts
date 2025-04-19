package dev.biserman.wingscontracts.registry

import com.mojang.brigadier.CommandDispatcher
import dev.architectury.event.events.common.CommandRegistrationEvent
import dev.biserman.wingscontracts.command.ModCommand
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object ModCommandRegistry {
    @JvmStatic
    fun register() {
        CommandRegistrationEvent.EVENT.register(CommandRegistrationEvent {
            dispatcher: CommandDispatcher<CommandSourceStack>,
            _: CommandBuildContext,
            _: Commands.CommandSelection? ->
                val contractNode = ModCommand.register().build()
                dispatcher.root.addChild(contractNode)
        })
    }
}
