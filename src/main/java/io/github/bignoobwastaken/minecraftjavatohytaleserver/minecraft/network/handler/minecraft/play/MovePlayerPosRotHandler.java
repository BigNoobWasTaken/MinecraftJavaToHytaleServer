package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.play;

import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.HytaleUtil;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket;

public class MovePlayerPosRotHandler implements PacketHandler<ServerboundMovePlayerPosRotPacket> {
    @Override
    public void handle(ServerboundMovePlayerPosRotPacket packet, MinecraftClientConnection connection) {
        Position position = new Position(packet.getX(), packet.getY(), packet.getZ());
        Direction lookAt = HytaleUtil.getHytaleDirection(packet.getYaw(), packet.getPitch());
        Direction bodyAt = new Direction(lookAt.yaw, 0, 0);

        connection.getMovementStates().jumping = !packet.isOnGround();
        connection.getMovementStates().onGround = packet.isOnGround();

        connection.checkLastCenterChunk(position.x, position.z);

        ClientMovement hytalePacket = new ClientMovement(connection.getMovementStates(), null, position, bodyAt, lookAt, null, null, null, 0, null);
        connection.getHytaleChannel().sendPacket(hytalePacket);
    }
}
