package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale;

import com.hypixel.hytale.protocol.CachedPacket;
import com.hypixel.hytale.protocol.ToClientPacket;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util.reflection.MethodAccessor;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util.reflection.Reflections;

import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.Map;

public class CachedPacketHandler<T extends ToClientPacket> implements PacketHandler<CachedPacket<T>> {
    private static final Map<Class<? extends ToClientPacket>, MethodAccessor> PACKET_DESERIALIZE_METHODS = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public void handle(CachedPacket<T> packet, MinecraftClientConnection connection) {
        Class<? extends ToClientPacket> packetType = packet.getPacketType();
        byte[] b = new byte[packet.getCachedSize()];
        MemorySegment ms = MemorySegment.ofArray(b);
        packet.serialize(ms, 0);

        MethodAccessor method = PACKET_DESERIALIZE_METHODS.get(packetType);
        if (method == null) {
            method = Reflections.getMethod(packetType, "toObject", MemorySegment.class, int.class);
            PACKET_DESERIALIZE_METHODS.put(packetType, method);
        }

        T packet1 = (T) method.invoke(null, ms, 0);
        HytaleClientPacketHandlerRegistry.INSTANCE.handlePacket(packet1, connection);
    }
}
