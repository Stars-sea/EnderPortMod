package com.github.stars_sea.enderport.item;

import com.github.stars_sea.enderport.util.PosNbtHelper;
import com.github.stars_sea.enderport.world.Location;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LocationRecordable extends Item {
    protected static final String LOCATION_NBT_KEY = "location";

    public LocationRecordable(Settings settings) {
        super(settings);
    }

    protected static NbtCompound getLocationNbt(@NotNull ItemStack stack) {
        return stack.getOrCreateSubNbt(LOCATION_NBT_KEY);
    }

    public static boolean hasRecorded(@NotNull ItemStack stack) {
        return PosNbtHelper.validLocationNbt(getLocationNbt(stack));
    }

    @Nullable
    public static Location getLocation(@NotNull ItemStack stack) {
        return PosNbtHelper.getLocation(getLocationNbt(stack));
    }

    public static void recordLocation(@NotNull ItemStack stack, @NotNull Location location) {
        stack.getOrCreateNbt().put(LOCATION_NBT_KEY, PosNbtHelper.getLocationNbt(location));
    }

    public static void clearLocation(@NotNull ItemStack stack) {
        stack.getOrCreateNbt().remove(LOCATION_NBT_KEY);
    }

    public ItemStack genStackWithLocation(int count, Location location) {
        ItemStack stack = new ItemStack(this, count);
        recordLocation(stack, location);
        return stack;
    }
}
