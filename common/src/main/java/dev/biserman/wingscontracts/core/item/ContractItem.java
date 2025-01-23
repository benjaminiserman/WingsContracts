package dev.biserman.wingscontracts.core.item;

import dev.biserman.wingscontracts.core.registry.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ContractItem extends Item {
    public ContractItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createContract(
        final String contractTargetItem,
        final String contractRewardItem,
        final int contractUnitPrice,
        final int countPerUnit,
        final int levelOneQuantity,
        final float quantityGrowthFactor,
        final int startLevel,
        final int maxLevel,
        final String author
    ) {
        var currentDay = (int)(System.currentTimeMillis() / 1000 / 60 / 60 / 24);
        var contractTag = new CompoundTag();
        contractTag.putString("contractTargetItem", contractTargetItem);
        contractTag.putString("contractRewardItem", contractRewardItem);
        contractTag.putInt("contractUnitPrice", contractUnitPrice);
        contractTag.putInt("countPerUnit", countPerUnit);
        contractTag.putInt("levelOneQuantity", levelOneQuantity);
        contractTag.putFloat("quantityGrowthFactor", quantityGrowthFactor);
        contractTag.putInt("startLevel", startLevel);
        contractTag.putInt("level", startLevel);
        contractTag.putInt("quantityDemanded", levelOneQuantity + (int)(levelOneQuantity * (startLevel - 1) * quantityGrowthFactor));
        contractTag.putInt("startDay", currentDay);
        contractTag.putInt("dateUpdated", currentDay);
        contractTag.putInt("quantityFulfilled", 0);
        contractTag.putInt("maxLevel", maxLevel);
        contractTag.putString("author", author);

        var contractItem = new ItemStack(ItemRegistry.CONTRACT.get());
        contractItem.addTagElement("contractInfo", contractTag);
        return contractItem;
    }
}
