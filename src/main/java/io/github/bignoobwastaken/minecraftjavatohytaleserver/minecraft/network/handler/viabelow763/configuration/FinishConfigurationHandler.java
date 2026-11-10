package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.viabelow763.configuration;

import com.viaversion.viaversion.api.connection.UserConnection;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.viabelow763.ViaPacketHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound.ClientboundFinishConfigurationPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundConfigurationAcknowledgedPacket;

public class FinishConfigurationHandler implements ViaPacketHandler<ClientboundFinishConfigurationPacket> {
    @Override
    public void handle(ClientboundFinishConfigurationPacket packet, Session session, UserConnection userConnection) {
        ServerboundConfigurationAcknowledgedPacket packet1 = new ServerboundConfigurationAcknowledgedPacket();
        ByteBuf byteBuf = Unpooled.buffer();
        int packetId = session.getPacketProtocol().getInboundPacketRegistry().getServerboundId(packet1);
        MinecraftTypes.writeVarInt(byteBuf, packetId);
        packet1.serialize(byteBuf);
        userConnection.sendRawPacketToServer(byteBuf);
    }
}
