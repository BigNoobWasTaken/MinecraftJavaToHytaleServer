package io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.api;

import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import com.hypixel.hytale.protocol.packets.player.ClientReady;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.ProtocolVersion;
import com.hypixel.hytale.server.core.io.handlers.SetupPacketHandler;
import com.hypixel.hytale.server.core.io.netty.NettyUtil;
import com.hypixel.hytale.server.core.io.netty.PlayerChannelHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.api.chunk.ChunkCoordIntPair;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.api.chunk.ChunkImpl;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.api.chunk.ChunkSectionImpl;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util.reflection.MethodAccessor;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.util.reflection.Reflections;
import io.netty.channel.*;
import lombok.Getter;
import lombok.Setter;

import java.net.SocketAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

@Getter
@Setter
public abstract class ClientConnection<S extends ChunkSectionImpl, C extends ChunkImpl<S>> {
    private static final MethodAccessor HANDLER_INIT_STAGE_METHOD = Reflections.getMethod(PacketHandler.class, "initStage", String.class, Duration.class, BooleanSupplier.class);
    private HytaleChannel hytaleChannel;

    private int networkId;
    private UUID nextWorldRespawn;

    private final ChunkBuffer chunkBuffer = new ChunkBuffer();

    private int viewDistance = 16;
    private float health;

    private boolean initialized;
    private final MovementStates movementStates = new MovementStates();

    private AtomicInteger clientChainId = new AtomicInteger(0);

    public class HytaleChannel extends AbstractChannel {
        private boolean closed;

        public HytaleChannel() {
            super(null);
        }

        @Override
        protected AbstractUnsafe newUnsafe() {
            return new FakeUnsafe();
        }

        @Override
        protected boolean isCompatible(EventLoop eventLoop) {
            return true;
        }

        @Override
        protected SocketAddress localAddress0() {
            return ClientConnection.this.getChannel().localAddress();
        }

        @Override
        protected SocketAddress remoteAddress0() {
            return ClientConnection.this.getChannel().remoteAddress();
        }

        @Override
        protected void doBind(SocketAddress socketAddress) throws Exception {
        }

        @Override
        protected void doDisconnect() throws Exception {
            ClientConnection.this.getChannel().close();
        }

        @Override
        protected void doClose() throws Exception {
            if (closed) {
                return;
            }
            closed = true;
            if (ClientConnection.this.getChannel().isOpen()) {
                ClientConnection.this.getChannel().close();
            }
        }

        @Override
        protected void doBeginRead() throws Exception {
        }

        @Override
        protected void doWrite(ChannelOutboundBuffer out) throws Exception {
            label:
            for (;;) {
                Object packet = out.current();
                switch (packet) {
                    case null:
                        break label;
                    case ToClientPacket[] hyPackages:
                        for (ToClientPacket hyPacket : hyPackages)
                            ClientConnection.this.handlePacket(hyPacket);
                        break;
                    case ToClientPacket hyPacket:
                        ClientConnection.this.handlePacket(hyPacket);
                        break;
                    default:
                        break;
                }
                out.remove();
            }
        }

        @Override
        public ChannelConfig config() {
            return ClientConnection.this.getChannel().config();
        }

        @Override
        public boolean isOpen() {
            return !this.closed;
        }

        @Override
        public boolean isActive() {
            return ClientConnection.this.getChannel().isActive() || !closed;
        }

        @Override
        public ChannelMetadata metadata() {
            return ClientConnection.this.getChannel().metadata();
        }

        public PacketHandler getPacketHandler() {
            ChannelHandler channelHandler = this.pipeline().get(NettyUtil.HANDLER);
            return channelHandler instanceof PlayerChannelHandler playerChannelHandler ? playerChannelHandler.getHandler() : null;
        }

        public void sendPacket(ToServerPacket packet) {
            PacketHandler handler = this.getPacketHandler();
            if (handler != null) {
                handler.handle(packet);
            }
        }

        private class FakeUnsafe extends AbstractUnsafe {
            @Override
            public void connect(SocketAddress remote, SocketAddress local, ChannelPromise promise) {
                promise.setSuccess();
            }
        }
    }

    protected abstract Channel getChannel();

    public class ChunkBuffer {
        private final Map<ChunkCoordIntPair, C> chunks = new HashMap<>();
        //private final Map<ChunkCoordIntPair, Integer> chunksYCoords = new HashMap<>();
        private final List<C> pendingChunks = new ArrayList<>();
        @Getter
        @Setter
        private ChunkCoordIntPair lastChunkSent;

        public void addChunkSection(ChunkCoordIntPair chunkCoords, int y, S section) {
            if (!chunks.containsKey(chunkCoords)) chunks.put(chunkCoords, ClientConnection.this.newChunk(chunkCoords));
            C chunk = chunks.get(chunkCoords);
            chunk.addChunkSection(section, y);
            if (ClientConnection.this.initialized) sendChunk(chunk);
            else pendingChunks.add(chunk);
        }

        public void removeChunk(ChunkCoordIntPair chunkCoords) {
            C chunk = chunks.remove(chunkCoords);
        }

        public void sendPendingChunks() {
            pendingChunks.forEach(ClientConnection.this::sendChunk);
        }
    }

    protected abstract C newChunk(ChunkCoordIntPair chunkCoords);

    protected abstract void handlePacket(ToClientPacket packet);

    protected abstract void sendChunk(C chunk);

    protected abstract UUID getUuid();

    protected abstract String getUsername();

    public PlayerRef getPlayerRef() {
        return Universe.get().getPlayer(this.getUuid());
    }

    @SuppressWarnings("resource")
    public final void createHytalePlayer() {
        HytaleChannel hyChannel = new HytaleChannel();
        ChannelFuture future = this.getChannel().eventLoop().register(hyChannel).syncUninterruptibly();
        this.hytaleChannel = hyChannel;
        PlayerAuthentication authentication = new PlayerAuthentication(this.getUuid(), this.getUsername());
        ProtocolVersion protocolVersion = new ProtocolVersion(-1356075132);
        SetupPacketHandler setupHandler = new SetupPacketHandler(
                new NettyUtil.NettyChannelConnection(hyChannel),
                protocolVersion,
                "en-us",
                authentication
        );
        hyChannel.pipeline().addLast(NettyUtil.HANDLER, new PlayerChannelHandler(setupHandler));

        HytaleServerConfig.TimeoutProfile timeouts = HytaleServer.get().getConfig().getConnectionTimeouts();
        HANDLER_INIT_STAGE_METHOD.invoke(setupHandler, "initial", timeouts.getInitial(), (BooleanSupplier) () -> true);
        setupHandler.registered(null);
        //Universe.get().addPlayer(new NettyUtil.NettyChannelConnection(), "en-us", protocolVersion, authentication, 0);
    }

    public void respawn(double x, double y, double z, float yaw, float pitch) {
        Direction direction = new Direction(yaw, pitch, 0);

        this.respawnSendPackets(x, y, z, direction);

        ClientReady hytaleClientReadyPacket = new ClientReady(false, true);
        this.getHytaleChannel().sendPacket(hytaleClientReadyPacket);

        this.movementStates.idle = true;
        this.movementStates.horizontalIdle = true;
        this.movementStates.onGround = true;

        this.changePlayerMiningSpeed();

        ClientMovement clientMovementPacket = new ClientMovement(movementStates, null, new Position(x, y, z), direction, direction, null, null, new Vector3d(0, 0, 0), 0, null);
        this.getHytaleChannel().sendPacket(clientMovementPacket);

        if (!this.initialized) {
            //List<String> message = HyCraft.get().getConfigManager().getMain().getJoinMessage();
            //this.inGameSystemMessage(message, "username", username);
            this.initialized = true;
        }
    }

    protected abstract void respawnSendPackets(double x, double y, double z, Direction direction);

    protected abstract void changePlayerMiningSpeed();
}
