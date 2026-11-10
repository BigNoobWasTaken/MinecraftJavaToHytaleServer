package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.play;

import com.hypixel.hytale.protocol.packets.interface_.ChatMessage;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;

public class ChatHandler implements PacketHandler<ServerboundChatPacket> {
    @Override
    public void handle(ServerboundChatPacket packet, MinecraftClientConnection connection) {
        ChatMessage messagePacket = new ChatMessage(packet.getMessage());
        connection.getHytaleChannel().sendPacket(messagePacket);
    }
}
