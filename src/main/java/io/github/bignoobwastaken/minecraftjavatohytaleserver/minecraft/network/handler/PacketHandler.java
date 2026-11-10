package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler;

import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;

public interface PacketHandler<P> {
    void handle(P packet, MinecraftClientConnection connection);
}
