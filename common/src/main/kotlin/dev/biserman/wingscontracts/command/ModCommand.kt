package dev.biserman.wingscontracts.command

import com.mojang.brigadier.builder.ArgumentBuilder
import dev.biserman.wingscontracts.WingsContractsMod
import dev.biserman.wingscontracts.api.Contract
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object ModCommand {
    fun register(): ArgumentBuilder<CommandSourceStack, *> =
        Commands.literal("contract")
            .requires { sourceStack: CommandSourceStack -> sourceStack.hasPermission(2) }
            .then(LoadContractCommand.register())
            .then(RandomContractCommand.register())
            .then(CopyContractCommand.register())

    fun giveContract(sourceStack: CommandSourceStack, contract: Contract?, players: Collection<ServerPlayer>): Int {
        if (contract == null) {
            sourceStack.sendFailure(Component.translatable("commands.${WingsContractsMod.MOD_ID}.failed.load"))
            return 0
        }

        for (player in players) {
            val itemStack = contract.createItem()
            val didAdd = player.inventory.add(itemStack)
            if (!didAdd) {
                val itemEntity = player.drop(itemStack, false)
                if (itemEntity != null) {
                    itemEntity.setNoPickUpDelay()
                    itemEntity.setTarget(player.uuid)
                }
            }
        }

        sourceStack.sendSuccess({ Component.translatable("commands.${WingsContractsMod.MOD_ID}.give_contract") }, true)

        return 1
    }
}