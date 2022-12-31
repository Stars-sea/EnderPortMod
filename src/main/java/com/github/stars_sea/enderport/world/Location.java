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

    public Location(@NotNull GlobalPos globalPos) {
        this(globalPos.getDimension(), globalPos.getPos());
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
            player.onLanding();
            return true;
        }

        LivingEntity entity1 = entity;
        if (isSameWorld(entity1.world)) {
            if (entity.moveToWorld(world) instanceof LivingEntity entityInOtherWorld)
                entity1 = entityInOtherWorld;
            else return false;
        }

        entity1.teleport(getX(), getY(), getZ());
        entity1.onLanding();
        return true;
    }

    @Nullable
    public Location teleportToNearbySafely(int offset, @NotNull LivingEntity entity, int times) {
        MinecraftServer server = entity.getServer();
        if (server == null) return null;

        Location nearby = getNearbySafeLocation(offset, server, times);
        return nearby.teleport(entity) ? nearby : null;
    }

    public Location getNearby(int offset) {
        Random random = new Random();
        double x = random.nextInt() % offset;
        double z = random.nextInt() % offset;

        return add(x, Math.abs(offset), z);
    }

    private boolean isSafe(@NotNull BlockPos.Mutable mutable, @NotNull World world) {
        return world.getBlockState(mutable).getMaterial().blocksMovement() &&
                world.getBlockState(mutable.add(0, 1, 0)).isAir() &&
                world.getBlockState(mutable.add(0, 2, 0)).isAir();
    }

    /**
     * 寻找周围安全的地点
     * @param offset 偏移量
     * @param server 服务器实例
     * @param times  尝试寻找次数 (-1 不限制)
     * @return 安全的地点, 如果超过 times, 则返回本身
     */
    @NotNull
    public Location getNearbySafeLocation(int offset, @NotNull MinecraftServer server, int times) {
        World world = server.getWorld(dimension);
        if (world == null) return this;

        BlockPos.Mutable mutable = getNearby(offset).mutable();
        int lowest = Math.max(world.getBottomY(), (int) getY() - offset);
        int flag   = 0;
        while (!isSafe(mutable, world) && (times == -1 || flag < times)) {
            if (mutable.getY() < lowest) {
                mutable = getNearby(offset).mutable();
                flag++;
                continue;
            }
            mutable.move(Direction.DOWN);
        }
        return flag <= times ? new Location(dimension, mutable.move(Direction.UP)) : this;
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
        return isSameWorld(dimension, world);
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
