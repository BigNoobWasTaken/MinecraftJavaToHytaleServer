package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.play;

import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;

public class MovePlayerPosHandler implements PacketHandler<ServerboundMovePlayerPosPacket> {
    @Override
    public void handle(ServerboundMovePlayerPosPacket packet, MinecraftClientConnection connection) {
        Position position = new Position(packet.getX(), packet.getY(), packet.getZ());

        connection.getMovementStates().jumping = !packet.isOnGround();
        connection.getMovementStates().onGround = packet.isOnGround();

        connection.checkLastCenterChunk(position.x, position.z);

        ClientMovement hytalePacket = new ClientMovement(connection.getMovementStates(), null, position, null, null, null, null, null, 0, null);
        connection.getHytaleChannel().sendPacket(hytalePacket);
    }
}
