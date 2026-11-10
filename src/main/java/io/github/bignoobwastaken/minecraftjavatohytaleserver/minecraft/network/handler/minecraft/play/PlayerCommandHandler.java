package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.play;

import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundPlayerCommandPacket;

public class PlayerCommandHandler implements PacketHandler<ServerboundPlayerCommandPacket> {
    @Override
    public void handle(ServerboundPlayerCommandPacket packet, MinecraftClientConnection connection) {
        MovementStates movementStates = connection.getMovementStates();

        switch (packet.getState()) {
            case START_SPRINTING -> movementStates.sprinting = true;
            case STOP_SPRINTING -> movementStates.sprinting = false;
            case START_HORSE_JUMP -> movementStates.jumping = true;
            case STOP_HORSE_JUMP -> movementStates.jumping = false;
            case START_ELYTRA_FLYING -> movementStates.gliding = true;
            case LEAVE_BED -> movementStates.sleeping = false;
            case OPEN_VEHICLE_INVENTORY -> {}
            default -> {}
        }

        ClientMovement hytalePacket = new ClientMovement(movementStates, null, null, null, null, null, null, null, 0, null);
        connection.getHytaleChannel().sendPacket(hytalePacket);
    }
}
