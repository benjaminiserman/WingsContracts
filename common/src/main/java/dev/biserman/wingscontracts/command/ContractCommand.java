package dev.biserman.wingscontracts.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.biserman.wingscontracts.item.ContractItem;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ContractCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext commandBuildContext) {
        return Commands.literal("contract")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("targetItem", ItemArgument.item(commandBuildContext))
                        .then(Commands.argument("unitPrice", IntegerArgumentType.integer()).then(Commands
                                .argument("levelOneQuantity", IntegerArgumentType.integer())
                                .then(Commands
                                        .argument("quantityGrowthFactor",
                                                FloatArgumentType.floatArg())
                                        .then(Commands.argument("maxLevel",
                                                IntegerArgumentType.integer())
                                                .then(Commands
                                                        .argument("rewardItem",
                                                                ItemArgument.item(commandBuildContext))
                                                        .then(Commands
                                                                .argument("countPerUnit", IntegerArgumentType.integer())

                                                                .executes(context -> {
                                                                    var targetItem = BuiltInRegistries.ITEM
                                                                            .getKey(ItemArgument
                                                                                    .getItem(context,
                                                                                            "targetItem")
                                                                                    .getItem())
                                                                            .toString();
                                                                    var rewardItem = BuiltInRegistries.ITEM
                                                                            .getKey(ItemArgument
                                                                                    .getItem(context,
                                                                                            "rewardItem")
                                                                                    .getItem())
                                                                            .toString();

                                                                    var unitPrice = IntegerArgumentType
                                                                            .getInteger(context, "unitPrice");
                                                                    var countPerUnit = IntegerArgumentType
                                                                            .getInteger(context, "countPerUnit");
                                                                    var levelOneQuantity = IntegerArgumentType
                                                                            .getInteger(context, "levelOneQuantity");
                                                                    var quantityGrowthFactor = FloatArgumentType
                                                                            .getFloat(context, "quantityGrowthFactor");
                                                                    var maxLevel = IntegerArgumentType
                                                                            .getInteger(context, "maxLevel");

                                                                    if (unitPrice <= 0) {
                                                                        context.getSource()
                                                                                .sendFailure(Component.literal(
                                                                                        "unitPrice must be greater than 0"));
                                                                    }

                                                                    if (countPerUnit <= 0) {
                                                                        context.getSource()
                                                                                .sendFailure(Component.literal(
                                                                                        "countPerUnit must be greater than 0"));
                                                                    }

                                                                    if (levelOneQuantity <= 0) {
                                                                        context.getSource()
                                                                                .sendFailure(Component.literal(
                                                                                        "levelOneQuantity must be greater than 0"));
                                                                    }

                                                                    if (quantityGrowthFactor < 0) {
                                                                        context.getSource()
                                                                                .sendFailure(Component.literal(
                                                                                        "quantityGrowthFactor must be non-negative"));
                                                                    }

                                                                    if (maxLevel <= 0 && maxLevel != -1) {
                                                                        context.getSource()
                                                                                .sendFailure(Component.literal(
                                                                                        "maxLevel must be -1 or greater than 0"));
                                                                    }

                                                                    var contract = ContractItem.createContract(
                                                                            targetItem,
                                                                            null,
                                                                            rewardItem,
                                                                            unitPrice,
                                                                            countPerUnit,
                                                                            levelOneQuantity,
                                                                            quantityGrowthFactor,
                                                                            1,
                                                                            maxLevel,
                                                                            context.getSource().getPlayer().getName()
                                                                                    .getString());

                                                                    return giveContract(context.getSource(), contract,
                                                                            context.getSource().getPlayer());
                                                                }))))))));
    }

    private static int giveContract(CommandSourceStack commandSourceStack, ItemStack contract,
            ServerPlayer serverPlayer) throws CommandSyntaxException {
        boolean didAdd = serverPlayer.getInventory().add(contract);
        if (didAdd && contract.isEmpty()) {
            contract.setCount(1);
            var itemEntity = serverPlayer.drop(contract, false);
            if (itemEntity != null) {
                itemEntity.makeFakeItem();
            }

            serverPlayer.level().playSound((Player) null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                    ((serverPlayer.getRandom().nextFloat() - serverPlayer.getRandom().nextFloat()) * 0.7F + 1.0F)
                            * 2.0F);
            serverPlayer.containerMenu.broadcastChanges();
        } else {
            var itemEntity = serverPlayer.drop(contract, false);
            if (itemEntity != null) {
                itemEntity.setNoPickUpDelay();
                itemEntity.setTarget(serverPlayer.getUUID());
            }
        }

        commandSourceStack.sendSuccess(() -> {
            return Component.translatable("commands.give.success.single",
                    new Object[] { 1, contract.getDisplayName(), serverPlayer.getDisplayName() });
        }, true);

        return 1;
    }
}
