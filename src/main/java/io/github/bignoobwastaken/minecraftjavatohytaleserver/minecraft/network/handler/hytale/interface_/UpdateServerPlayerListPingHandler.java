package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.interface_;

import com.hypixel.hytale.protocol.packets.interface_.UpdateServerPlayerListPing;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntry;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntryAction;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoUpdatePacket;

import java.util.ArrayList;
import java.util.EnumSet;

public class UpdateServerPlayerListPingHandler implements PacketHandler<UpdateServerPlayerListPing> {
    @Override
    public void handle(UpdateServerPlayerListPing packet, MinecraftClientConnection connection) {
        ArrayList<PlayerListEntry> playerInfos = new ArrayList<>();
        packet.players.forEach((uuid, ping) -> {
            PlayerListEntry playerInfo = new PlayerListEntry(uuid);
            playerInfo.setLatency(ping);

            playerInfos.add(playerInfo);
        });

        ClientboundPlayerInfoUpdatePacket addPlayerPacket = new ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(PlayerListEntryAction.UPDATE_LATENCY),
                playerInfos.toArray(new PlayerListEntry[0])
        );

        connection.getSession().send(addPlayerPacket);
    }
}
