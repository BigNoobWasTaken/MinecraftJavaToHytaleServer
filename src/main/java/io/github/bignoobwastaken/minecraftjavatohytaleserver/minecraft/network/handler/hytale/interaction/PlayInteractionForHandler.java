package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.hytale.interaction;

import com.hypixel.hytale.protocol.packets.interaction.PlayInteractionFor;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.PacketHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.data.entity.Entity;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Animation;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundAnimatePacket;

public class PlayInteractionForHandler implements PacketHandler<PlayInteractionFor> {
    private static final long INTERACTION_INTERVAL = 500L;

    @Override
    public void handle(PlayInteractionFor packet, MinecraftClientConnection connection) {
        /*System.out.println("sent play interaction for");
        System.out.println("interaction type. " + packet.interactionType);
        System.out.println("interaction id: " + packet.interactionId);

        Interaction interaction = Interaction.getAssetMap().getAsset(packet.interactionId);
        }*/
        Entity entity = connection.getSpawnedEntity(packet.entityId);
        if (entity == null) return;

        long current = System.currentTimeMillis();
        long last = connection.getLastAnimation();

        if (current - last < INTERACTION_INTERVAL) return;

        Animation animation;

        switch (packet.interactionType) {
            case Primary -> animation = Animation.SWING_ARM;
            case Secondary -> animation = Animation.SWING_OFFHAND;
            default -> {
                return;
            }
        }

        connection.setLastAnimation(current);

        ClientboundAnimatePacket animationPacket = new ClientboundAnimatePacket(packet.entityId, animation);
        connection.getSession().send(animationPacket);
    }
}
