package dev.biserman.wingscontracts.core.item;

import dev.biserman.wingscontracts.core.registry.ItemRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ContractItem extends Item {
    public ContractItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createContract(
        final String contractTargetItem,
        final String contractTargetTag,
        final String contractRewardItem,
        final int contractUnitPrice,
        final int countPerUnit,
        final int levelOneQuantity,
        final float quantityGrowthFactor,
        final int startLevel,
        final int maxLevel,
        final String author
    ) {
        var contractTag = new CompoundTag();
        contractTag.putString("contractTargetItem", contractTargetItem);
        contractTag.putString("contractTargetTag", contractTargetTag);
        contractTag.putString("contractRewardItem", contractRewardItem);
        contractTag.putInt("contractUnitPrice", contractUnitPrice);
        contractTag.putInt("countPerUnit", countPerUnit);
        contractTag.putInt("levelOneQuantity", levelOneQuantity);
        contractTag.putFloat("quantityGrowthFactor", quantityGrowthFactor);
        contractTag.putInt("startLevel", startLevel);
        contractTag.putInt("level", startLevel);
        contractTag.putInt("quantityDemanded", calculateQuantityDemanded(levelOneQuantity, startLevel, quantityGrowthFactor));
        contractTag.putLong("startHour", System.currentTimeMillis());
        contractTag.putLong("lastCycleStart", System.currentTimeMillis());
        contractTag.putLong("contractPeriod", 1000L * 60 * 60 * 24 * 7);
        contractTag.putInt("quantityFulfilled", 0);
        contractTag.putInt("maxLevel", maxLevel);
        contractTag.putString("author", author);

        var contractItem = new ItemStack(ItemRegistry.CONTRACT.get());
        contractItem.addTagElement("contractInfo", contractTag);
        return contractItem;
    }

    public static int calculateQuantityDemanded(int levelOneQuantity, int startLevel, float quantityGrowthFactor) {
        return levelOneQuantity + (int)(levelOneQuantity * (startLevel - 1) * quantityGrowthFactor);
    }

    public static void tick(ItemStack contract) {
        var currentTime = System.currentTimeMillis();
        var contractTag = contract.getTagElement("contractInfo");

        var lastCycleStart = contractTag.getLong("timeUpdated");
        var contractPeriod = contractTag.getLong("contractPeriod");
        var cyclesPassed = (int)((currentTime - lastCycleStart) / contractPeriod);
        if (cyclesPassed > 0) {
            update(contract, cyclesPassed);
        }
    }

    public static void update(ItemStack contract, int cycles) {
        var contractTag = contract.getTagElement("contractInfo");

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
            contractTag.getFloat("quantityGrowthFactor")
        ));

        contractTag.putInt("quantityFulfilled", 0);
    }

    public static boolean matches(ItemStack contract, ItemStack itemStack) {
        var contractTargetTag = contract.getTagElement("contractInfo").getString("contractTargetTag");
        var tagKey = TagKey.create(Registries.ITEM, ResourceLocation.tryParse(contractTargetTag));
        if (!contractTargetTag.equals("")) {
            return itemStack.is(tagKey);
        }

        var contractTargetItem = contract.getTagElement("contractInfo").getString("contractTargetItem");
        return itemStack.getItem().getDescriptionId().equals(contractTargetItem);
    }

    public static int remainingQuantity(ItemStack contract) {
        var quantityDemanded = contract.getTagElement("contractInfo").getInt("quantityDemanded");        
        var quantityFulfilled = contract.getTagElement("contractInfo").getInt("quantityFulfilled");
        
        return quantityDemanded - quantityFulfilled;
    }

    public static int consume(ItemStack contract, ItemStack itemStack) {
        var remainingQuantity = remainingQuantity(contract);
        if (remainingQuantity > 0 && matches(contract, itemStack)) {
            var amountConsumed = Math.min(itemStack.getCount(), remainingQuantity);
            itemStack.setCount(itemStack.getCount() - amountConsumed);
            var contractTag = contract.getTagElement("contractInfo");
            contractTag.putInt("quantityFulfilled", contractTag.getInt("quantityFulfilled") + amountConsumed);
            itemStack.addTagElement("contractInfo", contractTag);
            return amountConsumed;
        }

        return 0;
    }
}
