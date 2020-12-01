package me.kelgors.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {

    public static int getInventoryMaterialCount(Inventory inventory, Material material) {
        final ItemStack[] items = inventory.getContents();
        int count = 0;
        for (ItemStack stack : items) {
            if (stack != null && stack.getType() == material) count += stack.getAmount();
        }
        return count;
    }

    public static int addMaterialCount(Inventory inventory, Material material, int count) {
        final int MAX_STACK_SIZE = material.getMaxStackSize();
        int added = 0;
        while (added < count) {
            ItemStack itemStack = new ItemStack(material, Math.min(count - added, MAX_STACK_SIZE));
            inventory.addItem(itemStack);
            added += itemStack.getAmount();
        }
        return added;
    }

    public static int removeMaterialCount(Inventory inventory, Material material, int count) {
        final ItemStack[] items = inventory.getContents();
        int removed = 0;
        for (ItemStack stack : items) {
            if (stack != null && stack.getType() == material) {
                if (removed + stack.getAmount() > count) {
                    stack.setAmount(count - removed);
                    removed = (count - removed);
                } else {
                    removed += stack.getAmount();
                    inventory.remove(stack);
                }
            }
        }
        return removed;
    }

    public static boolean hasEnoughSpace(Inventory inventory, Material material, int spaceNeeded) {
        final ItemStack[] items = inventory.getContents();
        final int MAX_STACK_SIZE = material.getMaxStackSize();
        int count = 0;
        for (ItemStack stack : items) {
            if (stack == null) count += MAX_STACK_SIZE;
            else if (stack.getType() == material && stack.getAmount() < MAX_STACK_SIZE) {
                count += MAX_STACK_SIZE - stack.getAmount();
            }
        }
        return count > spaceNeeded;
    }

}
