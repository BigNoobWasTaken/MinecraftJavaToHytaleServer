package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.viabelow763;

import com.viaversion.viaversion.api.connection.UserConnection;
import org.geysermc.mcprotocollib.network.Session;

public interface ViaPacketHandler<P> {
    void handle(P packet, Session session, UserConnection userConnection);
}
