package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.server.core.io.netty.NettyUtil;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.api.ClientConnection;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.HytaleClientPacketHandlerRegistry;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.chunk.Chunk;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.hytaledev.api.chunk.ChunkCoordIntPair;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.chunk.ChunkSection;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.entity.Entity;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.gui.ContainerGui;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.HytaleUtil;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.blockbreak.BlockBreakManager;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.blockbreak.BlockBreakTracker;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.inventory.InventoryCursor;
import io.netty.channel.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerSpawnInfo;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import org.geysermc.mcprotocollib.protocol.data.game.level.notify.GameEvent;
import org.geysermc.mcprotocollib.protocol.data.game.level.notify.RespawnScreenValue;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundCommandsPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundRespawnPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundGameEventPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetChunkCacheCenterPacket;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.util.*;
import java.util.logging.Level;

@Getter
@Setter
public class MinecraftClientConnection extends ClientConnection<ChunkSection, Chunk> {
    private String pendingUsername;
    private byte[] pendingVerifyToken;

    private final Session session;

    //private final MinecraftServerAdditionalInfo serverInfo;
    private final HytaleLogger logger;

    private long lastAnimation;

    private String lastAttackInteraction;
    private long lastBreakTime = 0;
    private InventoryCursor cursor = new InventoryCursor();
    private Map<Integer, Entity> spawnedEntities = new HashMap<>();

    private Map<String, Long> itemIdsCooldowns = new HashMap<>();
    private final Map<Byte, Position> tpConfirmations = new HashMap<>();
    private BlockBreakTracker blockBreakTracker;

    private InventorySection cachedHotbar;
    private InventorySection cachedStorage;
    private InventorySection cachedArmor;

    private ContainerGui openedGui;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final KeyPair keyPair;

    public MinecraftClientConnection(Session session) {
        this.session = session;
        this.logger = HytaleLogger.getLogger();
        //this.serverInfo = serverInfo;
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            this.keyPair = generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String computeConnectionHash(byte[] sharedSecret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update("".getBytes("ISO_8859_1"));
            digest.update(sharedSecret);
            digest.update(this.keyPair.getPublic().getEncoded());
            return new BigInteger(digest.digest()).toString(16);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to compute server hash", e);
        }
    }

    public void disconnect(String reason) {
        this.logger.at(Level.FINE).log(reason);
        this.cleanup();
        MinecraftConnectionMap.INSTANCE.removeProfile(this.getUuid());
        this.session.disconnect(reason);
    }

    public void cleanup() {
        if (this.getHytaleChannel() != null && this.getHytaleChannel().isOpen()) {
            NettyUtil.closeApplicationConnection(this.getHytaleChannel());
        }
        if (this.blockBreakTracker != null) this.blockBreakTracker.cancel();
        this.setHytaleChannel(null);
    }

    public void checkLastCenterChunk(double x, double z) {
        ChunkCoordIntPair newChunk = Chunk.getChunkCoords(x, z);
        if (newChunk.equals(this.getChunkBuffer().getLastChunkSent())) return;

        ClientboundSetChunkCacheCenterPacket centerChunkPacket = new ClientboundSetChunkCacheCenterPacket(newChunk.getX(), newChunk.getZ());
        this.session.send(centerChunkPacket);
        this.getChunkBuffer().setLastChunkSent(newChunk);
    }

    @Override
    protected void respawnSendPackets(double x, double y, double z, Direction direction) {
        HytaleUtil.MinecraftYawPitch minecraftYawPitch = HytaleUtil.getMinecraftYawPitch(direction);

        if (this.getNextWorldRespawn() != null) {
            PlayerSpawnInfo playerSpawnInfo = new PlayerSpawnInfo(
                    0,
                    Key.key(HytaleUtil.getDimensionName(this.getNextWorldRespawn())),
                    0,
                    GameMode.SURVIVAL,
                    GameMode.SURVIVAL,
                    false,
                    false,
                    null,
                    0,
                    0
            );
            ClientboundRespawnPacket respawnPacket = new ClientboundRespawnPacket(
                    playerSpawnInfo,
                    true,
                    true
            );

            this.session.send(respawnPacket);
            this.setNextWorldRespawn(null);
        }

        ClientboundPlayerPositionPacket positionPacket = new ClientboundPlayerPositionPacket(
                1,
                x, y, z,
                0, 0, 0,
                minecraftYawPitch.mcYaw(), minecraftYawPitch.mcPitch()
        );
        this.session.send(positionPacket);

        ClientboundGameEventPacket minecraftReadyChunksPacket = new ClientboundGameEventPacket(GameEvent.from(13), RespawnScreenValue.from(0));
        this.session.send(minecraftReadyChunksPacket);

        ClientboundCommandsPacket commandsPacket = new ClientboundCommandsPacket(new MinecraftCommandsConverter().getCommandNodes(), 0);
        this.session.send(commandsPacket);

        ChunkCoordIntPair chunkCoords = Chunk.getChunkCoords(x, z);
        ClientboundSetChunkCacheCenterPacket centerChunkPacket = new ClientboundSetChunkCacheCenterPacket(chunkCoords.getX(), chunkCoords.getZ());
        this.session.send(centerChunkPacket);
        this.getChunkBuffer().setLastChunkSent(chunkCoords);

        this.getChunkBuffer().sendPendingChunks();
    }

    @Override
    protected void changePlayerMiningSpeed() {
        BlockBreakManager.INSTANCE.changePlayerMiningSpeed(this);
    }

    public void inGameSystemMessage(String message, String... vars) {
        if (message == null) return;
        message = parse(message, vars);
        message = colorize(message);
        ClientboundSystemChatPacket messagePacket = new ClientboundSystemChatPacket(Component.text(message), false);
        this.session.send(messagePacket);
    }

    public void inGameSystemMessage(List<String> messages, String... vars) {
        if (messages == null || messages.isEmpty()) return;
        messages.forEach(message -> this.inGameSystemMessage(message, vars));
    }

    public static String parse(String message, String... vars) {
        message = colorize(message);

        for (int i = 0; i < vars.length; i += 2) {
            message = message.replace("{" + vars[i] + "}", vars[i + 1]);
        }

        return message;
    }

    public static String colorize(String message) {
        return message.replaceAll("&([0-9a-fk-or])", "§$1");
    }

    @Deprecated
    @Override
    protected Channel getChannel() {
        return this.session.getChannel();
    }

    @Override
    protected Chunk newChunk(ChunkCoordIntPair chunkCoords) {
        return new Chunk(chunkCoords);
    }

    @Override
    protected void handlePacket(ToClientPacket packet) {
        HytaleClientPacketHandlerRegistry.INSTANCE.handlePacket(packet, this);
    }

    @Override
    protected void sendChunk(Chunk chunk) {
        int worldHeight = 384;
        int sectionsCount = worldHeight / 16;
        int totalSections = sectionsCount + 2;
        BitSet skyY = new BitSet(totalSections);
        BitSet blockY = new BitSet(0);
        BitSet emptySkyY = new BitSet(totalSections);
        BitSet emptyBlockY = new BitSet(totalSections);
        List<byte[]> skyUpdates = new ArrayList<>(sectionsCount);
        List<byte[]> blockUpdates = new ArrayList<>(0);

        for (int i = 0; i < totalSections; i++) {
            if (i == 0 || i == totalSections - 1) emptyBlockY.set(i, true);
            else skyY.set(i, true);
            emptyBlockY.set(i, true);
        }

        for (int i = 0; i < sectionsCount; i++) {
            byte [] a = new byte[2048];
            Arrays.fill(a, (byte) 0xFF);
            skyUpdates.add(a);
        }

        LightUpdateData lightData = new LightUpdateData(
                skyY,
                blockY,
                emptySkyY,
                emptyBlockY,
                skyUpdates,
                blockUpdates
        );

        ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(
                chunk.getChunkCoords().getX(),
                chunk.getChunkCoords().getZ(),
                chunk.getChunkSectionData(),
                new HashMap<>(),
                new BlockEntityInfo[0],
                lightData
        );
        this.session.send(packet);
    }

    @Override
    protected UUID getUuid() {
        return this.session.getFlag(MinecraftConstants.PROFILE_KEY).getId();
    }

    @Override
    protected String getUsername() {
        return this.session.getFlag(MinecraftConstants.PROFILE_KEY).getName();
    }

    public void addSpawnedEntity(int id, Entity entity) {
        spawnedEntities.put(id, entity);
    }

    public boolean isEntitySpawned(int id) {
        return spawnedEntities.containsKey(id);
    }

    public Map<Integer, Entity> getEntities() {
        return Collections.unmodifiableMap(getSpawnedEntities());
    }

    public Entity getSpawnedEntity(int id) {
        return spawnedEntities.get(id);
    }

    public void removeEntities(int[] ids) {
        for (int id : ids) spawnedEntities.remove(id);
    }
}
