package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.viabelow763;

import com.viaversion.viaversion.api.connection.UserConnection;
import org.geysermc.mcprotocollib.network.Session;

import java.util.HashMap;
import java.util.Map;

public class ViaHandlerRegistry<T> {
    private final Map<Class<? extends T>, ViaPacketHandler<? extends T>> packetHandlerMap = new HashMap<>();

    protected ViaHandlerRegistry() {}

    @SuppressWarnings("unchecked")
    public final <P extends T> void handlePacket(P packet, Session session, UserConnection connection) {
        ViaPacketHandler<P> handler = (ViaPacketHandler<P>) packetHandlerMap.get(packet.getClass());
        if (handler == null)
            return;
        this.handle(handler, packet, session, connection);
    }

    protected final boolean hasHandler(Class<? extends T> clazz) {
        return packetHandlerMap.containsKey(clazz);
    }

    protected final <P extends T> void addHandler(Class<P> clazz, ViaPacketHandler<P> handler) {
        packetHandlerMap.put(clazz, handler);
    }

    private <P extends T> void handle(ViaPacketHandler<P> handler, P packet, Session session, UserConnection connection) {
        handler.handle(packet, session, connection);
    }
}
