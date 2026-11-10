package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.Universe;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.auth.SessionService;
import org.geysermc.mcprotocollib.network.Server;
import org.geysermc.mcprotocollib.network.server.NetworkServer;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodec;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;

public class MinecraftServer implements AutoCloseable {
    private final Server server;
    {
        SessionService sessionService = new SessionService();
        sessionService.setProxy(null);

        SocketAddress ADDRESS = new InetSocketAddress(25565);
        server = new NetworkServer(ADDRESS, MinecraftProtocol::new);
        server.setGlobalFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);
        server.setGlobalFlag(MinecraftConstants.ENCRYPT_CONNECTION, true);
        server.setGlobalFlag(MinecraftConstants.SHOULD_AUTHENTICATE, true);
        server.setGlobalFlag(MinecraftConstants.SERVER_INFO_BUILDER_KEY, session ->
                new ServerStatusInfo(
                        Component.text(HytaleServer.get().getConfig().getMotd()),
                        new PlayerInfo(HytaleServer.get().getConfig().getMaxPlayers(), Universe.get().getPlayerCount(), new ArrayList<>()),
                        new VersionInfo(MinecraftCodec.CODEC.getMinecraftVersion(), MinecraftCodec.CODEC.getProtocolVersion()),
                        null,
                        false
                )
        );

//        server.setGlobalFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY, session ->
//                session.send(new ClientboundLoginPacket(
//                        0,
//                        false,
//                        new Key[]{Key.key("minecraft:world")},
//                        0,
//                        16,
//                        16,
//                        false,
//                        false,
//                        false,
//                        new PlayerSpawnInfo(
//                                0,
//                                Key.key("minecraft:world"),
//                                100,
//                                GameMode.SURVIVAL,
//                                GameMode.SURVIVAL,
//                                false,
//                                false,
//                                null,
//                                100,
//                                5
//                        ),
//                        false
//                ))
//        );
        server.setGlobalFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, 256);

        server.addListener(new MinecraftCrossplayServerAdapter());

    }

    public void bindServer() {
        server.bind();
    }

    @Override
    public void close() {
        server.close();
    }
}
