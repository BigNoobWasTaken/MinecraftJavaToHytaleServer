package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft;

import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.HandlerRegistry;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.play.*;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundKeepAlivePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClickPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClosePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level.ServerboundAcceptTeleportationPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level.ServerboundPlayerInputPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.*;
import testhytalecrossplayproject.minecraftjavatohytale.minecraft.network.handler.minecraft.play.*;

public class MinecraftServerPacketHandlerRegistry extends HandlerRegistry<Packet> {
    {
        // Play
        this.addHandler(ServerboundContainerClickPacket.class, new ContainerClickHandler());
        this.addHandler(ServerboundKeepAlivePacket.class, new KeepAliveHandler());
        this.addHandler(ServerboundPlayerActionPacket.class, new PlayerActionHandler());
        this.addHandler(ServerboundPlayerCommandPacket.class, new PlayerCommandHandler());
        this.addHandler(ServerboundPlayerInputPacket.class, new PlayerInputHandler());
        this.addHandler(ServerboundSetCarriedItemPacket.class, new SetCarriedItemHandler());
        this.addHandler(ServerboundMovePlayerPosRotPacket.class, new MovePlayerPosRotHandler());
        this.addHandler(ServerboundMovePlayerPosPacket.class, new MovePlayerPosHandler());
        this.addHandler(ServerboundMovePlayerRotPacket.class, new MovePlayerRotHandler());
        this.addHandler(ServerboundSwingPacket.class, new SwingHandler());
        this.addHandler(ServerboundAcceptTeleportationPacket.class, new AcceptTeleportationHandler());
        this.addHandler(ServerboundUseItemOnPacket.class, new UseItemOnHandler());
        //this.addHandler(ClientCommandPacket.class, new ClientCommandHandler());
        this.addHandler(ServerboundChatPacket.class, new ChatHandler());
        this.addHandler(ServerboundChatCommandPacket.class, new ChatCommandHandler());
        this.addHandler(ServerboundInteractPacket.class, new InteractHandler());
        this.addHandler(ServerboundContainerClosePacket.class, new ContainerCloseHandler());
    }

    public static final MinecraftServerPacketHandlerRegistry INSTANCE = new MinecraftServerPacketHandlerRegistry();
}
