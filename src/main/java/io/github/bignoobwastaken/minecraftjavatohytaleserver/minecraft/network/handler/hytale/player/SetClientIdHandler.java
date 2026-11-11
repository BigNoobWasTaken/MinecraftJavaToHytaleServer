package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.player;

import com.hypixel.hytale.protocol.packets.player.SetClientId;
import com.hypixel.hytale.server.core.universe.Universe;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.HytaleUtil;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerSpawnInfo;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;

import java.util.HashSet;
import java.util.Set;

public class SetClientIdHandler implements PacketHandler<SetClientId> {
    @Override
    public void handle(SetClientId packet, MinecraftClientConnection connection) {
        connection.setNetworkId(packet.clientId);
        if (connection.isInitialized()) return;

        ClientboundLoginPacket loginPacket = getLoginPlayPacket(connection, HytaleUtil.getDimensionName(connection.getNextWorldRespawn()));
        connection.getSession().send(loginPacket);
        connection.setNextWorldRespawn(null);
    }

    private static ClientboundLoginPacket getLoginPlayPacket(MinecraftClientConnection connection, String dimension) {
        Set<String> dimensions = Universe.get().getWorlds().keySet();

        PlayerSpawnInfo playerSpawnInfo =  new PlayerSpawnInfo(
                0,
                Key.key(dimension),
                0,
                GameMode.SURVIVAL,
                GameMode.SURVIVAL,
                false,
                false,
                null,
                0,
                0
        );

        Set<Key> keyArrayList = new HashSet<>();
        dimensions.forEach(dim -> keyArrayList.add(Key.key(dim)));

        return new ClientboundLoginPacket(
                connection.getNetworkId(),
                false,
                keyArrayList.toArray(new Key[0]),
                0,
                32,
                32,
                false,
                false,
                false,
                playerSpawnInfo,
                false
        );
    }
}
