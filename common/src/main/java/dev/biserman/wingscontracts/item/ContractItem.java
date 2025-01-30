package dev.biserman.wingscontracts.item;

import dev.biserman.wingscontracts.registry.ItemRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ContractItem extends Item {
    public ContractItem(Properties properties) {
        super(properties);
    }

    @Override
    // TODO: how do I localize this properly? e.g.: Contract de Niveau 10 des
    // Diamants de winggar
    public Component getName(ItemStack itemStack) {
        var contractTag = getBaseTag(itemStack);
        var author = contractTag.getString("author");
        var level = contractTag.getInt("level");
        var targetItem = contractTag.getString("targetItem");
        var targetTag = contractTag.getString("targetTag");

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
            var targetItemDisplayName = Component.translatable(BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(targetItem)).getDescriptionId()).getString();
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
            contractTag.putString("targetItem", targetItem);
        }

        if (targetTag != null) {
            contractTag.putString("targetTag", targetTag);
        }

        contractTag.putString("rewardItem", rewardItem);
        contractTag.putInt("unitPrice", unitPrice);
        contractTag.putInt("countPerUnit", countPerUnit);
        contractTag.putInt("levelOneQuantity", levelOneQuantity);
        contractTag.putFloat("quantityGrowthFactor", quantityGrowthFactor);
        contractTag.putInt("startLevel", startLevel);
        contractTag.putInt("level", startLevel);
        contractTag.putInt("quantityDemanded",
                calculateQuantityDemanded(levelOneQuantity, startLevel, quantityGrowthFactor, countPerUnit));
        contractTag.putLong("startHour", System.currentTimeMillis());
        contractTag.putLong("lastCycleStart", System.currentTimeMillis());
        contractTag.putLong("contractPeriodMs", 1000L * 60 * 60 * 24 * 7);
        contractTag.putInt("quantityFulfilled", 0);
        contractTag.putInt("maxLevel", maxLevel);
        contractTag.putString("author", author);

        var contractItem = new ItemStack(ItemRegistry.CONTRACT.get());
        contractItem.addTagElement("contractInfo", contractTag);
        return contractItem;
    }

    public static CompoundTag getBaseTag(ItemStack contract) {
        return contract.getTagElement("contractInfo");
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

        var lastCycleStart = contractTag.getLong("timeUpdated");
        var contractPeriod = contractTag.getLong("contractPeriod");
        var cyclesPassed = (int) ((currentTime - lastCycleStart) / contractPeriod);
        if (cyclesPassed > 0) {
            update(contract, cyclesPassed);
        }
    }

    public static void update(ItemStack contract, int cycles) {
        var contractTag = getBaseTag(contract);
        if (contractTag == null) {
            return;
        }

        var quantityDemanded = contractTag.getInt("quantityDemanded");
        var quantityFulfilled = contractTag.getInt("quantityFulfilled");
        if (quantityFulfilled >= quantityDemanded) {
            var currentLevel = contractTag.getInt("level");
            var maxLevel = contractTag.getInt("maxLevel");

            if (currentLevel < maxLevel) {
                contractTag.putInt("level", currentLevel + 1);
            }
        }

        contractTag.putInt("quantityDemanded", calculateQuantityDemanded(
                contractTag.getInt("levelOneQuantity"),
                contractTag.getInt("startLevel"),
                contractTag.getFloat("quantityGrowthFactor"),
                contractTag.getInt("countPerUnit")));

        contractTag.putInt("quantityFulfilled", 0);
    }

    public static boolean matches(ItemStack contract, ItemStack itemStack) {
        var targetTag = getBaseTag(contract).getString("targetTag");
        var tagKey = TagKey.create(Registries.ITEM, ResourceLocation.tryParse(targetTag));
        if (!targetTag.equals("")) {
            return itemStack.is(tagKey);
        }

        var targetItem = getBaseTag(contract).getString("targetItem");
        return itemStack.getItem().getDescriptionId().equals(targetItem);
    }

    public static int remainingQuantity(ItemStack contract) {
        var quantityDemanded = getBaseTag(contract).getInt("quantityDemanded");
        var quantityFulfilled = getBaseTag(contract).getInt("quantityFulfilled");

        return quantityDemanded - quantityFulfilled;
    }

    public static ItemStack targetItem(ItemStack contract) {
        var contractTag = getBaseTag(contract);
        if (contractTag == null) {
            return null;
        }

        var targetItem = contractTag.getString("targetItem");
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(targetItem)));
    }

    public static int consume(ItemStack contract, ItemStack itemStack) {
        var remainingQuantity = remainingQuantity(contract);
        if (remainingQuantity > 0 && matches(contract, itemStack)) {
            var amountConsumed = Math.min(itemStack.getCount(), remainingQuantity);
            itemStack.setCount(itemStack.getCount() - amountConsumed);
            var contractTag = getBaseTag(contract);
            contractTag.putInt("quantityFulfilled", contractTag.getInt("quantityFulfilled") + amountConsumed);
            itemStack.addTagElement("contractInfo", contractTag);
            return amountConsumed;
        }

        return 0;
    }
}
