package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.play;

import com.hypixel.hytale.protocol.packets.interface_.ChatMessage;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;

public class ChatCommandHandler implements PacketHandler<ServerboundChatCommandPacket> {
    @Override
    public void handle(ServerboundChatCommandPacket packet, MinecraftClientConnection connection) {
        ChatMessage messagePacket = new ChatMessage("/"+packet.getCommand());
        connection.getHytaleChannel().sendPacket(messagePacket);
    }
}
