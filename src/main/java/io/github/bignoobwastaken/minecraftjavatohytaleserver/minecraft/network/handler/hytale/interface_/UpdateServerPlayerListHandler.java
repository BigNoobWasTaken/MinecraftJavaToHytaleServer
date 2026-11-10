package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.interface_;

import com.hypixel.hytale.protocol.packets.interface_.UpdateServerPlayerList;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;

public class UpdateServerPlayerListHandler implements PacketHandler<UpdateServerPlayerList> {
    @Override
    public void handle(UpdateServerPlayerList packet, MinecraftClientConnection connection) {

    }
}
