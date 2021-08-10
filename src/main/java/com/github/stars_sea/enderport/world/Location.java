package com.github.stars_sea.enderport.world;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public final record Location(RegistryKey<World> dimension, Vec3d pos) {

    public Location(RegistryKey<World> dimension, Vec3i pos) {
        this(dimension, Vec3d.ofCenter(pos));
    }

    @NotNull
    public String getDimensionName() {
        return dimension.getValue().toString();
    }

    public double getX() {
        return pos.x;
    }

    public double getY() {
        return pos.y;
    }

    public double getZ() {
        return pos.z;
    }

    public String toString(boolean withDimension) {
        return withDimension
                ? String.format("%s [%.2f %.2f %.2f]", getDimensionName(), getX(), getY(), getZ())
                : String.format("[%.2f %.2f %.2f]", getX(), getY(), getZ());
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public boolean teleport(@NotNull PlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;

        ServerWorld world = server.getWorld(dimension);
        if (world == null) return false;

        if (player instanceof ServerPlayerEntity serverPlayer)
            serverPlayer.teleport(world, getX(), getY(), getZ(), player.headYaw, player.prevPitch);
        return true;
    }

    @Nullable
    public Location teleportToNearbySafely(int offset, @NotNull PlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return null;

        Location nearby = getNearbySafeLocation(offset, server);
        if (nearby != null && nearby.teleport(player))
            return nearby;

        return null;
    }

    public Location getNearby(int offset) {
        Random random = new Random();
        double x = random.nextInt() % offset;
        double y = random.nextInt() % offset;
        double z = random.nextInt() % offset;

        return add(x, Math.abs(y), z);
    }

    @Nullable
    public Location getNearbySafeLocation(int offset, @NotNull MinecraftServer server) {
        BlockPos.Mutable mutable = getNearby(offset).mutable();

        World world = server.getWorld(dimension);
        if (world == null) return null;

        while(mutable.getY() > world.getBottomY() && !world.getBlockState(mutable).getMaterial().blocksMovement())
            mutable.move(Direction.DOWN);
        mutable.move(Direction.UP);

        return new Location(dimension, mutable);
    }

    public Location add(double x, double y, double z) {
        if (x == 0 && y == 0 && z == 0)
            return this;
        return new Location(dimension, pos.add(x, y, z));
    }

    @NotNull @Contract(" -> new")
    public BlockPos.Mutable mutable() {
        return new BlockPos.Mutable(getX(), getY(), getZ());
    }
}
