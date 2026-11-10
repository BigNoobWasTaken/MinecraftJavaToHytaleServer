package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.player;

import com.hypixel.hytale.protocol.packets.player.ClientTeleport;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.HytaleUtil;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;

public class ClientTeleportHandler implements PacketHandler<ClientTeleport> {
    @Override
    public void handle(ClientTeleport packet, MinecraftClientConnection connection) {
        assert packet.modelTransform.position != null;
        double x = packet.modelTransform.position.x;
        double y = packet.modelTransform.position.y;
        double z = packet.modelTransform.position.z;

        assert packet.modelTransform.bodyOrientation != null;
        float yaw = HytaleUtil.getMinecraftYaw(packet.modelTransform.bodyOrientation.yaw);
        assert packet.modelTransform.lookOrientation != null;
        float pitch = HytaleUtil.getMinecraftPitch(packet.modelTransform.lookOrientation.pitch);

        connection.checkLastCenterChunk(x, z);

        ClientboundPlayerPositionPacket tpPacket = new ClientboundPlayerPositionPacket(
                packet.teleportId,
                x, y, z,
                0, 0, 0,
                yaw, pitch
        );
        connection.getSession().send(tpPacket);

        connection.getTpConfirmations().put(packet.teleportId, packet.modelTransform.position);
    }
}
