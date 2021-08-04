package com.github.stars_sea.enderport.util;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemHelper {
    public static float getUseProgress(int ticks, @NotNull ItemStack stack) {
        float p = ticks / (float) stack.getMaxUseTime();
        return Math.min(p, 1F);
    }
}
