package com.github.stars_sea.enderport.network.server.listener;

import com.github.stars_sea.enderport.EnderPortMod;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class EnderPortServerListeners {
    public static final Identifier RENAME_ENDER_SCROLL_PACKET_ID = EnderPortMod.genId("network.rename_ender_scroll");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(RENAME_ENDER_SCROLL_PACKET_ID, new RenameEnderScrollChannelHandler());
    }
}
