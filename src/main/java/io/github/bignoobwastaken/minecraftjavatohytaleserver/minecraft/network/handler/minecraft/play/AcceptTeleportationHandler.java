package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.play;

import com.hypixel.hytale.protocol.TeleportAck;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level.ServerboundAcceptTeleportationPacket;

public class AcceptTeleportationHandler implements PacketHandler<ServerboundAcceptTeleportationPacket> {
    @Override
    public void handle(ServerboundAcceptTeleportationPacket packet, MinecraftClientConnection connection) {
        byte id = (byte) packet.getId();
        if (connection.getTpConfirmations().get(id) == null) return;

        ClientMovement ackTpPacket = new ClientMovement();
        ackTpPacket.teleportAck = new TeleportAck(id);
        ackTpPacket.absolutePosition = connection.getTpConfirmations().get(id);
        connection.getHytaleChannel().sendPacket(ackTpPacket);

        connection.getTpConfirmations().remove(id);
    }
}
