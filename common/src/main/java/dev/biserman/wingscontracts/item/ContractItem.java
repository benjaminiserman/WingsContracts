package dev.biserman.wingscontracts.item;

import java.util.List;

import dev.biserman.wingscontracts.registry.ItemRegistry;
import dev.biserman.wingscontracts.util.ContractKey;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ContractItem extends Item {
    public ContractItem(Properties properties) {
        super(properties);
    }

    @Override
    // TODO: how do I localize this properly?
    // e.g.: Contract de Niveau 10 des Diamants de winggar
    public Component getName(ItemStack itemStack) {
        var contractTag = getBaseTag(itemStack);
        var author = contractTag.getString(ContractKey.author);
        var level = contractTag.getInt(ContractKey.level);
        var targetItem = contractTag.getString(ContractKey.targetItem);
        var targetTag = contractTag.getString(ContractKey.targetTag);

        var stringBuilder = new StringBuilder();
        if (author != null) {
            stringBuilder.append(author);
            stringBuilder.append("'s ");
        }

        if (level == -1) {
            stringBuilder.append("Endless ");
        } else {
            stringBuilder.append("Level ");
            stringBuilder.append(String.valueOf(level));
            stringBuilder.append(" ");
        }

        if (targetItem != null) {
            var targetItemDisplayName = Component
                    .translatable(BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(targetItem)).getDescriptionId())
                    .getString();
            stringBuilder.append(targetItemDisplayName);
            stringBuilder.append(" ");
        }

        if (targetTag != null) {
            var split = targetTag.split(":");
            if (split.length >= 2) {
                var tagName = split[1];
                stringBuilder.append(tagName.substring(0, 1).toUpperCase());
                stringBuilder.append(tagName.substring(1));
                stringBuilder.append(" ");
            }
        }

        stringBuilder.append(Component.translatable(getDescriptionId()).getString());

        return Component.literal(stringBuilder.toString());
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Level level, List<Component> components, TooltipFlag tooltipFlag) {
        components.add(Component.literal(""));
        if (Screen.hasShiftDown()) {
            components.add(Component.literal(""));
        }
    }

    public static ItemStack createContract(
            final String targetItem,
            final String targetTag,
            final String rewardItem,
            final int unitPrice,
            final int countPerUnit,
            final int levelOneQuantity,
            final float quantityGrowthFactor,
            final int startLevel,
            final int maxLevel,
            final String author) {
        var contractTag = new CompoundTag();

        if (rewardItem != null) {
            contractTag.putString(ContractKey.targetItem, targetItem);
        }

        if (targetTag != null) {
            contractTag.putString(ContractKey.targetTag, targetTag);
        }

        contractTag.putString(ContractKey.rewardItem, rewardItem);
        contractTag.putInt(ContractKey.unitPrice, unitPrice);
        contractTag.putInt(ContractKey.countPerUnit, countPerUnit);
        contractTag.putInt(ContractKey.levelOneQuantity, levelOneQuantity);
        contractTag.putFloat(ContractKey.quantityGrowthFactor, quantityGrowthFactor);
        contractTag.putInt(ContractKey.startLevel, startLevel);
        contractTag.putInt(ContractKey.level, startLevel);
        contractTag.putInt(ContractKey.quantityDemanded,
                calculateQuantityDemanded(levelOneQuantity, startLevel, quantityGrowthFactor, countPerUnit));
        contractTag.putLong(ContractKey.startTime, System.currentTimeMillis());
        contractTag.putLong(ContractKey.currentCycleStart, System.currentTimeMillis());
        contractTag.putLong(ContractKey.cycleDurationMs, 1000L * 60 * 60 * 24 * 7);
        contractTag.putInt(ContractKey.quantityFulfilled, 0);
        contractTag.putInt(ContractKey.maxLevel, maxLevel);
        contractTag.putString(ContractKey.author, author);

        var contractItem = new ItemStack(ItemRegistry.CONTRACT.get());
        contractItem.addTagElement(ContractKey.contractInfo, contractTag);
        return contractItem;
    }

    public static CompoundTag getBaseTag(ItemStack contract) {
        return contract.getTagElement(ContractKey.contractInfo);
    }

    public static int calculateQuantityDemanded(int levelOneQuantity, int startLevel, float quantityGrowthFactor,
            int countPerUnit) {
        var quantity = levelOneQuantity + (int) (levelOneQuantity * (startLevel - 1) * quantityGrowthFactor);
        var quantizedQuantity = quantity - quantity % countPerUnit;
        return quantizedQuantity;
    }

    public static void tick(ItemStack contract) {
        var currentTime = System.currentTimeMillis();
        var contractTag = getBaseTag(contract);
        if (contractTag == null) {
            return;
        }

        var currentCycleStart = contractTag.getLong(ContractKey.currentCycleStart);
        var contractPeriod = contractTag.getLong(ContractKey.cycleDurationMs);
        var cyclesPassed = (int) ((currentTime - currentCycleStart) / contractPeriod);
        if (cyclesPassed > 0) {
            update(contract, cyclesPassed);
        }
    }

    public static void update(ItemStack contract, int cycles) {
        var contractTag = getBaseTag(contract);
        if (contractTag == null) {
            return;
        }

        var quantityDemanded = contractTag.getInt(ContractKey.quantityDemanded);
        var quantityFulfilled = contractTag.getInt(ContractKey.quantityFulfilled);
        if (quantityFulfilled >= quantityDemanded) {
            var currentLevel = contractTag.getInt(ContractKey.level);
            var maxLevel = contractTag.getInt(ContractKey.maxLevel);

            if (currentLevel < maxLevel) {
                contractTag.putInt(ContractKey.level, currentLevel + 1);
            }
        }

        contractTag.putInt(ContractKey.quantityDemanded, calculateQuantityDemanded(
                contractTag.getInt(ContractKey.levelOneQuantity),
                contractTag.getInt(ContractKey.startLevel),
                contractTag.getFloat(ContractKey.quantityGrowthFactor),
                contractTag.getInt(ContractKey.countPerUnit)));

        contractTag.putInt(ContractKey.quantityFulfilled, 0);
    }

    public static boolean matches(ItemStack contract, ItemStack itemStack) {
        var targetTag = getBaseTag(contract).getString(ContractKey.targetTag);
        var tagKey = TagKey.create(Registries.ITEM, ResourceLocation.tryParse(targetTag));
        if (!targetTag.equals("")) {
            return itemStack.is(tagKey);
        }

        var targetItem = getBaseTag(contract).getString(ContractKey.targetItem);
        return itemStack.getItem().getDescriptionId().equals(targetItem);
    }

    public static int remainingQuantity(ItemStack contract) {
        var quantityDemanded = getBaseTag(contract).getInt(ContractKey.quantityDemanded);
        var quantityFulfilled = getBaseTag(contract).getInt(ContractKey.quantityFulfilled);

        return quantityDemanded - quantityFulfilled;
    }

    public static ItemStack targetItem(ItemStack contract) {
        var contractTag = getBaseTag(contract);
        if (contractTag == null) {
            return null;
        }

        var targetItem = contractTag.getString(ContractKey.targetItem);
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(targetItem)));
    }

    public static int consume(ItemStack contract, ItemStack itemStack) {
        var remainingQuantity = remainingQuantity(contract);
        if (remainingQuantity > 0 && matches(contract, itemStack)) {
            var amountConsumed = Math.min(itemStack.getCount(), remainingQuantity);
            itemStack.setCount(itemStack.getCount() - amountConsumed);
            var contractTag = getBaseTag(contract);
            contractTag.putInt(ContractKey.quantityFulfilled, contractTag.getInt(ContractKey.quantityFulfilled) + amountConsumed);
            itemStack.addTagElement(ContractKey.contractInfo, contractTag);
            return amountConsumed;
        }

        return 0;
    }
}
