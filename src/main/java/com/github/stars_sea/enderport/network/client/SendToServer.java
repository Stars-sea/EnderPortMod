package com.github.stars_sea.enderport.network.client;

import com.github.stars_sea.enderport.network.EnderPortChannels;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public final class SendToServer {
    public static void sendRenameEnderScroll(String name, boolean isChanged) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(name).writeBoolean(isChanged);
        ClientPlayNetworking.send(EnderPortChannels.C2S_RENAME_ENDER_SCROLL, buf);
    }
}
