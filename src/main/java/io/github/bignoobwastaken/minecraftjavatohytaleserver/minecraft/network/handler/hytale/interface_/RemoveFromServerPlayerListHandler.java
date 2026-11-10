package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.interface_;

import com.hypixel.hytale.protocol.packets.interface_.RemoveFromServerPlayerList;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoRemovePacket;

import java.util.Arrays;

public class RemoveFromServerPlayerListHandler implements PacketHandler<RemoveFromServerPlayerList> {
    @Override
    public void handle(RemoveFromServerPlayerList packet, MinecraftClientConnection connection) {
        ClientboundPlayerInfoRemovePacket removePacket = new ClientboundPlayerInfoRemovePacket(Arrays.stream(packet.players).toList());
        connection.getSession().send(removePacket);
    }
}
