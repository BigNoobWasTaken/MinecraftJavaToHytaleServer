package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler;

import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;

import java.util.HashMap;
import java.util.Map;

public class HandlerRegistry<T> {
    private final Map<Class<? extends T>, PacketHandler<? extends T>> packetHandlerMap = new HashMap<>();

    protected HandlerRegistry() {}

    @SuppressWarnings("unchecked")
    public final <P extends T> void handlePacket(P packet, MinecraftClientConnection connection) {
        PacketHandler<P> handler = (PacketHandler<P>) packetHandlerMap.get(packet.getClass());
        if (handler == null)
            return;
        this.handle(handler, packet, connection);
    }

    protected final boolean hasHandler(Class<? extends T> clazz) {
        return packetHandlerMap.containsKey(clazz);
    }

    protected final <P extends T> void addHandler(Class<P> clazz, PacketHandler<P> handler) {
        packetHandlerMap.put(clazz, handler);
    }

    private <P extends T> void handle(PacketHandler<P> handler, P packet, MinecraftClientConnection connection) {
        handler.handle(packet, connection);
    }
}
