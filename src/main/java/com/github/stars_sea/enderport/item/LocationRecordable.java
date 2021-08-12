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
        return stack.getItem() instanceof LocationRecordable &&
                PosNbtHelper.validLocationNbt(getLocationNbt(stack));
    }

    @Nullable
    public static Location getLocation(@NotNull ItemStack stack) {
        return hasRecorded(stack) ? PosNbtHelper.getLocation(getLocationNbt(stack)) : null;
    }

    public static boolean recordLocation(@NotNull ItemStack stack, @NotNull Location location) {
        if (stack.getItem() instanceof LocationRecordable) {
            stack.getOrCreateNbt().put(LOCATION_NBT_KEY, PosNbtHelper.getLocationNbt(location));
            return true;
        }
        return false;
    }

    public static void clearLocation(@NotNull ItemStack stack) {
        if (hasRecorded(stack))
            stack.getOrCreateNbt().remove(LOCATION_NBT_KEY);
    }

    public ItemStack genStackWithLocation(int count, Location location) {
        ItemStack stack = new ItemStack(this, count);
        recordLocation(stack, location);
        return stack;
    }
}
