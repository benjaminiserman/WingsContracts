package dev.biserman.wingscontracts.core.block;

import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

import dev.biserman.wingscontracts.core.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.state.BlockState;

public class ContractPortalBlockEntity extends BaseContainerBlockEntity implements Hopper {
    private int cooldownTime;
    private NonNullList<ItemStack> items;

    public ContractPortalBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityRegistry.CONTRACT_PORTAL.get(), blockPos, blockState);
        this.items = NonNullList.withSize(54, ItemStack.EMPTY);
        this.cooldownTime = -1;
    }

    protected Component getDefaultName() {
        return Component.translatable("container.wingscontracts.contractportal");
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compoundTag, items);
        this.cooldownTime = compoundTag.getInt("SuckCooldown");
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        ContainerHelper.saveAllItems(compoundTag, items);
        compoundTag.putInt("SuckCooldown", this.cooldownTime);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    public static boolean serverTick(Level level, BlockPos blockPos, BlockState blockState,
            ContractPortalBlockEntity contractPortalBlockEntity) {
        --contractPortalBlockEntity.cooldownTime;
        if (!contractPortalBlockEntity.isOnCooldown()) {
            contractPortalBlockEntity.setCooldown(0);
            if (!contractPortalBlockEntity.inventoryFull()) {

                if (suckInItems(level, contractPortalBlockEntity)) {
                    contractPortalBlockEntity.setCooldown(10);
                    setChanged(level, blockPos, blockState);
                    return true;
                }
            }
        }

        return false;
    }

    public static List<ItemEntity> getItemsAtAndAbove(Level level, ContractPortalBlockEntity portal) {
        return portal.getSuckShape().toAabbs().stream().flatMap(aABB -> {
            return level.getEntitiesOfClass(ItemEntity.class,
                    aABB.move(portal.getLevelX() - 0.5D, portal.getLevelY() - 0.5D, portal.getLevelZ() - 0.5D),
                    EntitySelector.ENTITY_STILL_ALIVE).stream();
        }).collect(Collectors.toList());
    }

    public static boolean suckInItems(Level level, ContractPortalBlockEntity portal) {
        var aboveItems = getItemsAtAndAbove(level, portal).iterator();

        ItemEntity itemEntity;
        do {
            if (!aboveItems.hasNext()) {
                return false;
            }

            itemEntity = (ItemEntity) aboveItems.next();
        } while (!addItem(portal, itemEntity));

        return true;
    }

    public static boolean addItem(ContractPortalBlockEntity portal, ItemEntity itemEntity) {
        boolean bl = false;
        ItemStack itemStack = itemEntity.getItem().copy();
        ItemStack itemStack2 = addItem(portal, itemStack);
        if (itemStack2.isEmpty()) {
            bl = true;
            itemEntity.discard();
        } else {
            itemEntity.setItem(itemStack2);
        }

        return bl;
    }

    public static ItemStack addItem(ContractPortalBlockEntity portal, ItemStack itemStack) {
        int size = portal.getContainerSize();

        for (int i = 0; i < size && !itemStack.isEmpty(); ++i) {
            itemStack = tryMoveInItem(portal, itemStack, i);
        }

        return itemStack;
    }

    private static ItemStack tryMoveInItem(ContractPortalBlockEntity portal, ItemStack itemStack, int i) {
        ItemStack itemStack2 = portal.getItem(i);
        if (portal.canPlaceItem(i, itemStack)) {
            boolean movedItems = false;
            if (itemStack2.isEmpty()) {
                portal.setItem(i, itemStack);
                itemStack = ItemStack.EMPTY;
                movedItems = true;
            } else if (canMergeItems(itemStack2, itemStack)) {
                int j = itemStack.getMaxStackSize() - itemStack2.getCount();
                int k = Math.min(itemStack.getCount(), j);
                itemStack.shrink(k);
                itemStack2.grow(k);
                movedItems = k > 0;
            }

            if (movedItems) {
                portal.setCooldown(10);
            }

            portal.setChanged();
        }

        return itemStack;
    }

    private static boolean canMergeItems(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack.getCount() <= itemStack.getMaxStackSize()
                && ItemStack.isSameItemSameTags(itemStack, itemStack2);
    }

    private void setCooldown(int i) {
        this.cooldownTime = i;
    }

    @Override
    public double getLevelX() {
        return (double) this.worldPosition.getX() + 0.5D;
    }

    @Override
    public double getLevelY() {
        return (double) this.worldPosition.getY() + 0.5D;
    }

    @Override
    public double getLevelZ() {
        return (double) this.worldPosition.getZ() + 0.5D;
    }

    private boolean isOnCooldown() {
        return this.cooldownTime > 0;
    }

    private boolean inventoryFull() {
        var iterator = this.items.iterator();

        ItemStack itemStack;
        do {
            if (!iterator.hasNext()) {
                return true;
            }

            itemStack = (ItemStack) iterator.next();
        } while (!itemStack.isEmpty() && itemStack.getCount() == itemStack.getMaxStackSize());

        return false;
    }

    public ItemStack removeItem(int i, int j) {
        return ContainerHelper.removeItem(this.getItems(), i, j);
    }

    public void setItem(int i, ItemStack itemStack) {
        this.getItems().set(i, itemStack);
        if (itemStack.getCount() > this.getMaxStackSize()) {
            itemStack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    public boolean isEmpty() {
        return this.getItems().stream().allMatch(ItemStack::isEmpty);
    }

    public ItemStack getItem(int i) {
        return (ItemStack) this.getItems().get(i);
    }

    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    public void clearContent() {
        this.getItems().clear();
    }

    public ItemStack removeItemNoUpdate(int i) {
        return ContainerHelper.takeItem(this.getItems(), i);
    }

    @Nullable
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        if (this.canOpen(player)) {
            return this.createMenu(i, inventory);
        } else {
            return null;
        }
    }

    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return null;
    }
}
