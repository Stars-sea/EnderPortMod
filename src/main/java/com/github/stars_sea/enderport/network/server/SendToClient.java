package com.github.stars_sea.enderport.network.server;

import com.github.stars_sea.enderport.network.EnderPortChannels;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class SendToClient {
    public static void sendOpenRenameScreen(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, EnderPortChannels.S2C_OPEN_RENAME_SCROLL, PacketByteBufs.empty());
    }
}
