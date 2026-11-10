package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.play;

import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundKeepAlivePacket;

public class KeepAliveHandler implements PacketHandler<ServerboundKeepAlivePacket> {
    @Override
    public void handle(ServerboundKeepAlivePacket packet, MinecraftClientConnection connection) {

    }
}
