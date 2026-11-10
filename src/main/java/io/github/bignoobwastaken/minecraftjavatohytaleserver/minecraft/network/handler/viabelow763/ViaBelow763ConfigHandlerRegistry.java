package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.viabelow763;

import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.viabelow763.configuration.FinishConfigurationHandler;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound.ClientboundFinishConfigurationPacket;

public class ViaBelow763ConfigHandlerRegistry extends ViaHandlerRegistry<Packet> {
    {
        //this.addHandler(ServerboundLoginAcknowledgedPacket.class, new LoginAcknowledgedHandler());
        this.addHandler(ClientboundFinishConfigurationPacket.class, new FinishConfigurationHandler());
    }

    public static final ViaBelow763ConfigHandlerRegistry INSTANCE = new ViaBelow763ConfigHandlerRegistry();
    private ViaBelow763ConfigHandlerRegistry() {}
}
