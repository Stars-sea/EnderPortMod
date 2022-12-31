package com.github.stars_sea.enderport.network.client.listener;

import com.github.stars_sea.enderport.network.EnderPortChannels;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class EnderPortClientListeners {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(
                EnderPortChannels.S2C_OPEN_RENAME_SCROLL,
                new OpenRenameScreenChannelListener()
        );
    }
}
