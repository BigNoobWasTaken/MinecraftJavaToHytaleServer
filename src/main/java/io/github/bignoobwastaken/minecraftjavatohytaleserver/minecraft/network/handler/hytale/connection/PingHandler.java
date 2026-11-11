package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.connection;

import com.hypixel.hytale.protocol.packets.connection.Ping;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;

public class PingHandler implements PacketHandler<Ping> {
    @Override
    public void handle(Ping packet, MinecraftClientConnection connection) {
        //connection.setPendingPing(packet);

        //ClientboundKeepAlivePacket keepAlivePacket = new ClientboundKeepAlivePacket(System.currentTimeMillis());
        //connection.getSession().send(keepAlivePacket);
    }
}
