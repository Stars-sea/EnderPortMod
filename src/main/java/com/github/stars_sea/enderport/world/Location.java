package com.github.stars_sea.enderport.world;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public record Location(RegistryKey<World> dimension, Vec3d pos) {

    public Location(RegistryKey<World> dimension, Vec3i pos) {
        this(dimension, Vec3d.ofCenter(pos));
    }

    public Location(@NotNull World world, Vec3i pos) {
        this(world.getRegistryKey(), pos);
    }

    public Location(@NotNull World world, Vec3d pos) {
        this(world.getRegistryKey(), pos);
    }

    @NotNull
    public String getDimensionName() {
        return dimension.getValue().toString();
    }

    @Nullable
    public ServerWorld getWorld(@NotNull MinecraftServer server) {
        return server.getWorld(dimension);
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

    public boolean teleport(@NotNull LivingEntity entity) {
        MinecraftServer server = entity.getServer();
        if (server == null) return false;

        ServerWorld world = getWorld(server);
        if (world == null) return false;
        if (world.isClient) return true;

        if (entity instanceof ServerPlayerEntity player) {
            player.teleport(world, getX(), getY(), getZ(), player.prevYaw, player.prevPitch);
            return true;
        }

        LivingEntity entity1 = entity;
        if (entity.world.getRegistryKey() != dimension) {
            if (entity.moveToWorld(world) instanceof LivingEntity living)
                entity1 = living;
            else return false;
        }

        entity1.teleport(getX(), getY(), getZ());
        return true;
    }

    @Nullable
    public Location teleportToNearbySafely(int offset, @NotNull LivingEntity entity) {
        MinecraftServer server = entity.getServer();
        if (server == null) return null;

        Location nearby = getNearbySafeLocation(offset, server);
        if (nearby != null && nearby.teleport(entity))
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

    @NotNull @Contract("_, _ -> new")
    public List<LivingEntity> getLivingEntitiesAround(@NotNull MinecraftServer server, int radius) {
        ServerWorld world = server.getWorld(dimension);
        if (world == null) return List.of();

        return world.getEntitiesByType(
                TypeFilter.instanceOf(LivingEntity.class), box(radius),
                entity -> pos.distanceTo(entity.getPos()) <= radius
        );
    }

    public static boolean isSameWorld(RegistryKey<World> regKey, @Nullable World world) {
        return world != null && regKey.getValue().equals(world.getRegistryKey().getValue());
    }

    public boolean isSameWorld(@Nullable World world) {
        return world != null && dimension.getValue().equals(world.getRegistryKey().getValue());
    }

    @NotNull @Contract(" -> new")
    public BlockPos.Mutable mutable() {
        return new BlockPos.Mutable(getX(), getY(), getZ());
    }

    @NotNull @Contract("_ -> new")
    public Box box(int a) {
        final double
                x = getX(),
                y = getY(),
                z = getZ();
        return new Box(
                x + a, y + a, z + a,
                x - a, y - a, z - a
        );
    }
}
