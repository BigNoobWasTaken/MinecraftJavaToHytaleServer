package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.world;

import com.hypixel.hytale.protocol.packets.world.UpdateTime;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetTimePacket;

public class UpdateTimeHandler implements PacketHandler<UpdateTime> {
    @Override
    public void handle(UpdateTime packet, MinecraftClientConnection connection) {
        if (packet.gameTime == null) return;

        long seconds = packet.gameTime.seconds;
        long secondsOfDay = ((seconds % 86400) + 86400) % 86400;
        long timeOfDay = ((secondsOfDay * 24000 / 86400) + 18000) % 24000;

        ClientboundSetTimePacket timePacket = new ClientboundSetTimePacket(0, timeOfDay, false);
        connection.getSession().send(timePacket);
    }
}
