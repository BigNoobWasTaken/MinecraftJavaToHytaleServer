package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.viabelow763;

import com.viaversion.viaversion.api.connection.UserConnection;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.packet.login.serverbound.ServerboundLoginAcknowledgedPacket;

public class LoginAcknowledgedHandler implements ViaPacketHandler<ServerboundLoginAcknowledgedPacket> {
    @Override
    public void handle(ServerboundLoginAcknowledgedPacket packet, Session session, UserConnection userConnection) {
    }
}
