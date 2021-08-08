package com.github.stars_sea.enderport.world;

import com.github.stars_sea.enderport.util.PosHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final record Location(RegistryKey<World> dimension, Vec3d pos) {

    public Location(@NotNull World world, BlockPos pos) {
        this(world.getRegistryKey(), Vec3d.of(pos));
    }

    @NotNull
    public String getDimensionName() {
        return dimension.getValue().toString();
    }

    /**
     * @return 如果 x 小数部分为 0, 则加上 0.5
     */
    public double getX() {
        double x = pos.x;
        return (x % 1 == 0) ? (x + 0.5) : x;
    }

    public double getY() {
        return pos.y;
    }

    /**
     * @return 如果 z 小数部分为 0, 则加上 0.5
     */
    public double getZ() {
        double z = pos.z;
        return (z % 1 == 0) ? (z + 0.5) : z;
    }

    @NotNull @Contract(" -> new")
    public NbtCompound getPosNbt() {
        return PosHelper.getNbt(pos);
    }

    @NotNull
    public NbtCompound getNbt() {
        return writeNbt(new NbtCompound());
    }

    @NotNull @Contract("_ -> param1")
    public NbtCompound writeNbt(@NotNull NbtCompound nbt) {
        nbt.putString("dimension", getDimensionName());
        nbt.put("pos", getPosNbt());
        return nbt;
    }

    @Nullable
    public static Location deserialize(@NotNull NbtCompound nbt) {
        if (!valid(nbt)) return null;

        Identifier         world = new Identifier(nbt.getString("dimension"));
        RegistryKey<World> key   = RegistryKey.of(Registry.WORLD_KEY, world);
        Vec3d              pos   = PosHelper.getPos(nbt.getCompound("pos"));
        return new Location(key, pos);
    }

    public static boolean valid(@NotNull NbtCompound nbt) {
        if (!nbt.contains("dimension", 8) || !nbt.contains("pos", 10))
            return false;

        String world = nbt.getString("dimension");
        NbtCompound pos = nbt.getCompound("pos");

        return Identifier.isValid(world) && PosHelper.validPos(pos);
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

    public Location add(double x, double y, double z) {
        if (x == 0 && y == 0 && z == 0)
            return this;
        return new Location(dimension, pos.add(x, y, z));
    }
}
