package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.world;

import com.hypixel.hytale.protocol.packets.world.UpdateBlockDamage;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.BlockBreakStage;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundBlockDestructionPacket;

import java.util.Objects;

public class UpdateBlockDamageHandler implements PacketHandler<UpdateBlockDamage> {
    @Override
    public void handle(UpdateBlockDamage packet, MinecraftClientConnection connection) {
        int destroyStage = calculateDestroyStage(packet.damage);

        assert packet.blockPosition != null;
        int sourceId = Objects.hash(packet.blockPosition.x, packet.blockPosition.y, packet.blockPosition.z);

        Vector3i blockPos = Vector3i.from(packet.blockPosition.x, packet.blockPosition.y, packet.blockPosition.z);

        ClientboundBlockDestructionPacket stagePacket = new ClientboundBlockDestructionPacket(sourceId, blockPos, BlockBreakStage.values()[destroyStage]);
        connection.getSession().send(stagePacket);
    }

    private static int calculateDestroyStage(float damage) {
        if (damage >= 0.95f) return 10;
        if (damage <= 0.0f) return 10;

        float damagePercent = 1.0f - damage;
        int stage = (int)(damagePercent * 10);

        return Math.clamp(stage, 0, 9);
    }
}
