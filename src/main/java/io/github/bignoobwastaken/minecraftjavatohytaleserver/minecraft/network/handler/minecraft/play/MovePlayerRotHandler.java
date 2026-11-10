package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.play;

import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.HytaleUtil;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerRotPacket;

public class MovePlayerRotHandler implements PacketHandler<ServerboundMovePlayerRotPacket> {
    @Override
    public void handle(ServerboundMovePlayerRotPacket packet, MinecraftClientConnection connection) {
        Direction lookAt = HytaleUtil.getHytaleDirection(packet.getYaw(), packet.getPitch());
        Direction bodyAt = new Direction(lookAt.yaw, 0, 0);

        connection.getMovementStates().jumping = !packet.isOnGround();
        connection.getMovementStates().onGround = packet.isOnGround();

        ClientMovement hytalePacket = new ClientMovement(connection.getMovementStates(), null, null, bodyAt, lookAt, null, null, null, 0, null);
        connection.getHytaleChannel().sendPacket(hytalePacket);
    }
}
