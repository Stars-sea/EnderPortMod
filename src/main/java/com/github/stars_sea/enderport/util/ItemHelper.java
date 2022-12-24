package com.github.stars_sea.enderport.util;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemHelper {
    public static float getUseProgress(int ticks, @NotNull ItemStack stack) {
        float p = ticks / (float) stack.getMaxUseTime();
        return Math.min(p, 1F);
    }

    public static void addToGroup(ItemConvertible item, ItemGroup @NotNull ... groups) {
        for (ItemGroup group : groups) {
            ItemGroupEvents.modifyEntriesEvent(group).register(e -> e.add(item));
        }
    }
}
