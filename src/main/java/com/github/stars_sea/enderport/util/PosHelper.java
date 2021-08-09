package com.github.stars_sea.enderport.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public final class PosHelper {

    @NotNull
    public static Vec3d getRandomPos(@NotNull Vec3d base) {
        Random random = new Random();
        double x = base.x + (random.nextDouble() - 0.5D) * 64.0D;
        double y = base.y + (random.nextInt(128));
        double z = base.z + (random.nextDouble() - 0.5D) * 64.0D;
        return new Vec3d(x, y, z);
    }

    public static BlockPos.Mutable getSafeMutable(@NotNull World world, @NotNull BlockPos.Mutable mutable) {
        while(mutable.getY() > world.getBottomY() && !world.getBlockState(mutable).getMaterial().blocksMovement() &&
                world.isAir(mutable.add(0, 1, 0))) {
            mutable.move(Direction.DOWN);
        }
        return mutable.move(Direction.UP);
    }

    @NotNull
    public static Vec3d getRandomSafePos(@NotNull World world, @NotNull Vec3d pos) {
        Vec3d curr = getRandomPos(pos);
        BlockPos.Mutable mutable = getSafeMutable(
                world, new BlockPos.Mutable(Math.round(curr.x), curr.y, Math.round(curr.z))
        );
        return new Vec3d(mutable.getX() + 0.5, mutable.getY(), mutable.getZ() + 0.5);
    }

    @NotNull
    public static NbtCompound getNbt(@NotNull Vec3d pos) {
        NbtCompound nbt = new NbtCompound();
        nbt.putDouble("X", pos.x);
        nbt.putDouble("Y", pos.y);
        nbt.putDouble("Z", pos.z);
        return nbt;
    }

    public static boolean validPos(@NotNull NbtCompound nbt) {
        return nbt.contains("X", 99) &&
                nbt.contains("Y", 99) &&
                nbt.contains("Z", 99);
    }

    @Nullable
    public static Vec3d getPos(@NotNull NbtCompound nbt) {
        if (!validPos(nbt)) return null;

        double x = nbt.getDouble("X");
        double y = nbt.getDouble("Y");
        double z = nbt.getDouble("Z");
        return new Vec3d(x, y, z);
    }
}
