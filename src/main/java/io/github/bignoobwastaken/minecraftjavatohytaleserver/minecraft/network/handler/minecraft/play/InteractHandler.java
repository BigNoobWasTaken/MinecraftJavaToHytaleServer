package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.play;

import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.entity.EntityManager;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
//import org.geysermc.mcprotocollib.protocol.data.game.entity.player.InteractAction;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundInteractPacket;

public class InteractHandler implements PacketHandler<ServerboundInteractPacket> {
    @Override
    public void handle(ServerboundInteractPacket packet, MinecraftClientConnection connection) {
        //if (!packet.getAction().equals(InteractAction.ATTACK)) return;
        EntityManager.INSTANCE.hitEntity(connection, packet.getEntityId());
    }
}
