package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.world;

import com.hypixel.hytale.protocol.packets.player.ClientReady;
import com.hypixel.hytale.protocol.packets.player.JoinWorld;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;

public class JoinWorldHandler implements PacketHandler<JoinWorld> {
    @Override
    public void handle(JoinWorld packet, MinecraftClientConnection connection) {
        connection.setNextWorldRespawn(packet.worldUuid);
        ClientReady hytaleClientReadyPacket = new ClientReady(true, false);
        connection.getHytaleChannel().sendPacket(hytaleClientReadyPacket);
    }
}
