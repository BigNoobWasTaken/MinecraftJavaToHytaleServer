package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.interface_;

import com.hypixel.hytale.protocol.packets.interface_.AddToServerPlayerList;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftConnectionMap;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntry;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntryAction;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoUpdatePacket;

import java.util.EnumSet;

public class AddToServerPlayerListHandler implements PacketHandler<AddToServerPlayerList> {
    @Override
    public void handle(AddToServerPlayerList packet, MinecraftClientConnection connection) {
        PlayerListEntry[] playerInfos = new PlayerListEntry[packet.players.length];
        for (int i = 0; i < packet.players.length; i++) {
            PlayerListEntry playerInfo = new PlayerListEntry(packet.players[i].uuid);
            playerInfo.setListed(true);
            playerInfo.setLatency(packet.players[i].ping);
            String username = packet.players[i].username;
            assert username != null;
            playerInfo.setDisplayName(Component.text(username));

            GameProfile playerConnection = MinecraftConnectionMap.INSTANCE.getProfile(packet.players[i].uuid);
            if (playerConnection != null) {
                playerInfo.setProfile(connection.getSession().getPacketProtocol().getProfile());
            } else {
                playerInfo.setProfile(new GameProfile(packet.players[i].uuid, username));
            }

            playerInfos[i] = playerInfo;
        }

        ClientboundPlayerInfoUpdatePacket addPlayerPacket = new ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(
                        PlayerListEntryAction.ADD_PLAYER,
                        PlayerListEntryAction.UPDATE_LISTED,
                        PlayerListEntryAction.UPDATE_LATENCY,
                        PlayerListEntryAction.UPDATE_DISPLAY_NAME
                ), playerInfos
        );

        connection.getSession().send(addPlayerPacket);
    }

    private String sanitizeName(String name) {
        if (name == null) return "";

        int space = name.lastIndexOf(' ');
        if (space != -1) {
            name = name.substring(space + 1);
        }

        if (name.length() > 16) {
            name = name.substring(0, 16);
        }

        return name;
    }
}
