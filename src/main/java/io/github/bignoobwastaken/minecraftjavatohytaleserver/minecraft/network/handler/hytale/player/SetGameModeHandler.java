package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.player;

import com.hypixel.hytale.protocol.packets.player.SetGameMode;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.level.notify.GameEvent;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundGameEventPacket;

public class SetGameModeHandler implements PacketHandler<SetGameMode> {
    @Override
    public void handle(SetGameMode packet, MinecraftClientConnection connection) {
        ClientboundGameEventPacket gameEventPacket = new ClientboundGameEventPacket(GameEvent.from(3), GameMode.byId(packet.gameMode.getValue()));
        connection.getSession().send(gameEventPacket);
    }
}
