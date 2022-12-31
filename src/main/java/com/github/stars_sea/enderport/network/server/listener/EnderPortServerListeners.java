package com.github.stars_sea.enderport.network.server.listener;

import com.github.stars_sea.enderport.network.EnderPortChannels;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class EnderPortServerListeners {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(
                EnderPortChannels.C2S_RENAME_ENDER_SCROLL,
                new RenameEnderScrollChannelHandler()
        );
    }
}
