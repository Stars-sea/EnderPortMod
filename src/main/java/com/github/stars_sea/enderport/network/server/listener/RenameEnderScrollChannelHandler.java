package com.github.stars_sea.enderport.network.server.listener;

import com.github.stars_sea.enderport.item.EnderPortItems;
import com.github.stars_sea.enderport.util.PosNbtHelper;
import com.github.stars_sea.enderport.world.Location;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class RenameEnderScrollChannelHandler implements ServerPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(
            MinecraftServer server,
            @NotNull ServerPlayerEntity player,
            ServerPlayNetworkHandler handler,
            @NotNull PacketByteBuf buf,
            PacketSender responseSender
    ) {
        buf.retain();
        String   name     = buf.readString();
        Location location = PosNbtHelper.getLocation(buf.readNbt());
        buf.release();

        EnderPortItems.ENDER_SCROLL.recordPos(player, location, name);
    }
}
