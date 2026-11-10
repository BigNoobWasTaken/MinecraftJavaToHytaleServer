package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.play;

import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level.ServerboundPlayerInputPacket;

public class PlayerInputHandler implements PacketHandler<ServerboundPlayerInputPacket> {
    @Override
    public void handle(ServerboundPlayerInputPacket packet, MinecraftClientConnection connection) {
        MovementStates movementStates = connection.getMovementStates();

        boolean hasHorizontalMovement = packet.isLeft()
                || packet.isRight()
                || packet.isForward()
                || packet.isBackward();

        movementStates.running = packet.isSprint();
        movementStates.walking = hasHorizontalMovement;
        movementStates.idle = !hasHorizontalMovement;
        movementStates.horizontalIdle = !hasHorizontalMovement;
        movementStates.crouching = packet.isShift();
        movementStates.jumping = packet.isJump();

        ClientMovement hytalePacket = new ClientMovement(movementStates, null, null, null, null, null, null, null, 0, null);
        connection.getHytaleChannel().sendPacket(hytalePacket);
    }
}
