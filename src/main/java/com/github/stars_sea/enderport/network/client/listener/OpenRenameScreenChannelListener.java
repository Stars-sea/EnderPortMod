package com.github.stars_sea.enderport.network.client.listener;

import com.github.stars_sea.enderport.client.gui.screen.ingame.NamingEnderScrollScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class OpenRenameScreenChannelListener implements ClientPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(
            @NotNull MinecraftClient client,
            ClientPlayNetworkHandler handler,
            PacketByteBuf buf,
            PacketSender responseSender
    ) {
        client.execute(() -> client.setScreen(new NamingEnderScrollScreen()));
    }
}
