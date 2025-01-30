package dev.biserman.wingscontracts.registry;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.biserman.wingscontracts.command.ContractCommand;

public class CommandRegistry {
    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, b) -> {
            var contractNode = ContractCommand.register(registryAccess).build();
            dispatcher.getRoot().addChild(contractNode);
        });
    }
}
