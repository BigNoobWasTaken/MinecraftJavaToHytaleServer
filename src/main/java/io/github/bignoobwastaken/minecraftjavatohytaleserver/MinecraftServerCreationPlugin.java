package io.github.bignoobwastaken.minecraftjavatohytaleserver;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.MinecraftServer;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util.ServerIconUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.io.File;
import java.io.IOException;

public class MinecraftServerCreationPlugin extends JavaPlugin {
    //public final HttpClient httpClient = HttpClient.newHttpClient();
    private static MinecraftServerCreationPlugin INSTANCE;
    private final MinecraftServer minecraftServer = new MinecraftServer();
    //private final HttpServerBootstrap httpBootstrap = new HttpServerBootstrap();
    public static MinecraftServerCreationPlugin getInstance() {
        return INSTANCE;
    }

    public MinecraftServerCreationPlugin(@NonNullDecl JavaPluginInit init) {
        super(init);
        INSTANCE = this;
    }

    @Override
    protected void setup() {
//        try {
//            ServerIconUtil.INSTANCE.loadServerIcon(new File(ModdingConstant.PATH + "/Image","MinecraftIcon.png"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

//        PacketAdapters.registerInbound((PacketWatcher) (packetHandler, packet) -> {
//            System.out.println("inbound packet track: " + packetHandler.getIdentifier() + ' ' + packet.getClass().getName());
//            if (!(packet instanceof SmartMoveItemStack movePacket)) return;
//
//            System.out.println("move packet: " + movePacket.fromSectionId);
//        });
//
//        PacketAdapters.registerOutbound((PacketWatcher) (packetHandler, packet) -> {
//            if (packet instanceof NetworkTick) return;
//            if (packet instanceof CachedPacket<?> cachedPacket)
//                System.out.println("outbound cached packet track: " + packetHandler.getIdentifier() + ' ' + cachedPacket.getPacketType().getName());
//            else
//                System.out.println("outbound raw    packet track: " + packetHandler.getIdentifier() + ' ' + packet.getClass().getName());
//        });
    }

    @Override
    protected void start() {
        minecraftServer.bindServer();
    }

    @Override
    protected void shutdown() {
        minecraftServer.close();
    }
}
