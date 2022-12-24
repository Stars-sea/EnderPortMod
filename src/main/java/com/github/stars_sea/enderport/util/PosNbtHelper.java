package com.github.stars_sea.enderport.util;

import com.github.stars_sea.enderport.world.Location;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PosNbtHelper {

    // Vec3d
    @NotNull
    public static NbtCompound getVec3dNbt(@NotNull Vec3d pos) {
        NbtCompound nbt = new NbtCompound();
        nbt.putDouble("X", pos.x);
        nbt.putDouble("Y", pos.y);
        nbt.putDouble("Z", pos.z);
        return nbt;
    }

    public static boolean validVec3dNbt(@NotNull NbtCompound nbt) {
        return nbt.contains("X", 99) &&
                nbt.contains("Y", 99) &&
                nbt.contains("Z", 99);
    }

    @Nullable
    public static Vec3d getVec3d(@NotNull NbtCompound nbt) {
        if (!validVec3dNbt(nbt)) return null;

        double x = nbt.getDouble("X");
        double y = nbt.getDouble("Y");
        double z = nbt.getDouble("Z");
        return new Vec3d(x, y, z);
    }

    // Location
    @NotNull
    public static NbtCompound getLocationNbt(@NotNull Location location) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("dimension", location.getDimensionName());
        nbt.put("pos", getVec3dNbt(location.pos()));
        return nbt;
    }

    public static boolean validLocationNbt(@NotNull NbtCompound nbt) {
        if (!nbt.contains("dimension", 8) || !nbt.contains("pos", 10))
            return false;

        String world = nbt.getString("dimension");
        NbtCompound pos = nbt.getCompound("pos");

        return Identifier.isValid(world) && PosNbtHelper.validVec3dNbt(pos);
    }

    @Nullable
    public static Location getLocation(NbtCompound nbt) {
        if (!validLocationNbt(nbt)) return null;

        Identifier         world = new Identifier(nbt.getString("dimension"));
        RegistryKey<World> key   = RegistryKey.of(RegistryKeys.WORLD, world);
        Vec3d              pos   = PosNbtHelper.getVec3d(nbt.getCompound("pos"));
        return new Location(key, pos);
    }
}
