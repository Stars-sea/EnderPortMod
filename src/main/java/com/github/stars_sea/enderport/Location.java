package com.github.stars_sea.enderport;

import com.github.stars_sea.enderport.util.PosHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final record Location(RegistryKey<World> world, Vec3d pos) {
    @NotNull
    public String getWorldName() {
        return world.getValue().toString();
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
        nbt.putString("world", getWorldName());
        nbt.put("pos", getPosNbt());
        return nbt;
    }

    @Nullable
    public static Location deserialize(@NotNull NbtCompound nbt) {
        if (!valid(nbt)) return null;

        Identifier         world = new Identifier(nbt.getString("world"));
        RegistryKey<World> key   = RegistryKey.of(Registry.WORLD_KEY, world);
        Vec3d              pos   = PosHelper.getPos(nbt.getCompound("pos"));
        return new Location(key, pos);
    }

    public static boolean valid(@NotNull NbtCompound nbt) {
        if (!nbt.contains("world", 8) || !nbt.contains("pos", 10))
            return false;

        String world = nbt.getString("world");
        NbtCompound pos = nbt.getCompound("pos");

        return Identifier.isValid(world) && PosHelper.validPos(pos);
    }

    public String toString(boolean withWorld) {
        return withWorld
                ? String.format("%s [%.2f %.2f %.2f]", getWorldName(), getX(), getY(), getZ())
                : String.format("[%.2f %.2f %.2f]", getX(), getY(), getZ());
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public boolean teleport(@NotNull PlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;

        ServerWorld world = server.getWorld(world());
        if (world == null) return false;

        if (player instanceof ServerPlayerEntity serverPlayer)
            serverPlayer.teleport(world, getX(), getY(), getZ(), player.headYaw, player.prevPitch);
        return true;
    }
}
