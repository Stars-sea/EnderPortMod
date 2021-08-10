package com.github.stars_sea.enderport.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemHelper {
    public static float getUseProgress(int ticks, @NotNull ItemStack stack) {
        float p = ticks / (float) stack.getMaxUseTime();
        return Math.min(p, 1F);
    }

    public static boolean tryGiveToPlayer(@NotNull PlayerEntity player, @NotNull ItemStack... stacks) {
        PlayerInventory inventory = player.getInventory();
        boolean noDrop = true;
        for (ItemStack stack : stacks) {
            if (inventory.getEmptySlot() != -1)
                player.giveItemStack(stack);
            else {
                player.dropItem(stack, true);
                noDrop = false;
            }
        }
        return noDrop;
    }
}
