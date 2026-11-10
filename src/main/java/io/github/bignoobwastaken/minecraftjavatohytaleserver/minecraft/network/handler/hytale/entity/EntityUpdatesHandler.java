package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.entity;

import com.hypixel.hytale.protocol.ComponentUpdateType;
import com.hypixel.hytale.protocol.EntityUpdate;
import com.hypixel.hytale.protocol.packets.entities.EntityUpdates;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.manager.entity.EntityManager;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;

public class EntityUpdatesHandler implements PacketHandler<EntityUpdates> {
    @Override
    public void handle(EntityUpdates packet, MinecraftClientConnection connection) {
        if (packet.updates == null) return;

        if (packet.removed != null && packet.removed.length > 0) EntityManager.INSTANCE.removeEntities(connection, packet.removed);

        for (EntityUpdate update : packet.updates) {
            if (update.removed != null) {
                for (ComponentUpdateType removedType : update.removed) {
                    //System.out.println("REMOVED :" + removedType.name());
                }
            }

            if (update.updates != null) EntityManager.INSTANCE.handleEntityUpdate(connection, update.networkId, update.updates);
        }
    }
}
