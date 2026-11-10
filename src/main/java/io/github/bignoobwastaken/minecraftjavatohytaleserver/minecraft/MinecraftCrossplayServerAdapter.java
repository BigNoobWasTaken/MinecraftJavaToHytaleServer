package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft;

import com.viaversion.viabackwards.ViaBackwardsPlatformImpl;
import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.commands.ViaCommandHandler;
import com.viaversion.viaversion.platform.NoopInjector;
import com.viaversion.viaversion.platform.ViaChannelInitializer;
import com.viaversion.viaversion.platform.ViaCodecHandler;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.handler.minecraft.MinecraftServerPacketHandlerRegistry;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.player.MinecraftClientConnection;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.viaversion.ViaHytalePlatform;
import io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.viaversion.ViaHytalePlatformLoader;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.NetworkConstants;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.server.ServerAdapter;
import org.geysermc.mcprotocollib.network.event.server.ServerClosedEvent;
import org.geysermc.mcprotocollib.network.event.server.SessionAddedEvent;
import org.geysermc.mcprotocollib.network.event.server.SessionRemovedEvent;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.PacketSendingEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundUpdateTagsPacket;
import org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound.ClientboundFinishConfigurationPacket;
import org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound.ClientboundRegistryDataPacket;
import org.geysermc.mcprotocollib.protocol.packet.configuration.serverbound.ServerboundFinishConfigurationPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MinecraftCrossplayServerAdapter extends ServerAdapter {
    private MinecraftClientConnection connection;

    static {
        ViaHytalePlatform hytalePlatform = new ViaHytalePlatform();
        ViaManagerImpl.initAndLoad(
                hytalePlatform,
                new NoopInjector() {
                    @Override
                    public String getEncoderName() {
                        return ViaCodecHandler.NAME;
                    }

                    @Override
                    public String getDecoderName() {
                        return ViaCodecHandler.NAME;
                    }
                },
                new ViaCommandHandler(false /* if true, a command for toggling check-for-updates in ViaVersion's config will be registered */),
                new ViaHytalePlatformLoader()
        );

        new ViaBackwardsPlatformImpl();
    }

    @Override
    public void serverClosed(ServerClosedEvent event) {
        System.out.println("Server closed.");
    }

    @Override
    public void sessionAdded(SessionAddedEvent event) {
        if (!Via.isLoaded()) event.getSession().disconnect("ViaVersion is not loaded");
        final UserConnection connection1 = ViaChannelInitializer.createUserConnection(event.getSession().getChannel(), false /* clientside */);
        final ViaInjector injector = Via.getManager().getInjector();
        event.getSession().getChannel().pipeline().addBefore(NetworkConstants.CODEC_NAME, ViaCodecHandler.NAME, new ViaCodecHandler(connection1));

        event.getSession().addListener(new SessionAdapter() {
            private boolean tagsUpdateDone;
            @Override
            public void packetReceived(Session session, Packet packet) {
                if (connection != null)
                    MinecraftServerPacketHandlerRegistry.INSTANCE.handlePacket(packet, MinecraftCrossplayServerAdapter.this.connection);
                switch (packet) {
                    case ServerboundChatPacket chatPacket -> {
                        GameProfile profile = session.getFlag(MinecraftConstants.PROFILE_KEY);
                        //log.info("{}: {}", profile.getName(), chatPacket.getMessage());

                        Component msg = Component.text("Hello, ")
                                .color(NamedTextColor.GREEN)
                                .append(Component.text(profile.getName())
                                        .color(NamedTextColor.AQUA)
                                        .decorate(TextDecoration.UNDERLINED))
                                .append(Component.text("!")
                                        .color(NamedTextColor.GREEN));

                        session.send(new ClientboundSystemChatPacket(msg, false));
                    }
                    case ServerboundFinishConfigurationPacket finishConfigurationPacket -> {
                        MinecraftCrossplayServerAdapter.this.connection = new MinecraftClientConnection(session);
                        MinecraftCrossplayServerAdapter.this.connection.createHytalePlayer();
                    }
                    default -> {}
                }
            }

            @Override
            public void packetSending(PacketSendingEvent event) {
                switch (event.getPacket()) {
                    case ClientboundRegistryDataPacket registryDataPacket -> {
                        if (Objects.equals(registryDataPacket.getRegistry(), Key.key("minecraft:enchantment")) ||
                                Objects.equals(registryDataPacket.getRegistry(), Key.key("minecraft:dialog")))
                            event.setCancelled(true);
                    }
                    case ClientboundFinishConfigurationPacket clientboundFinishConfigurationPacket -> {
                        if (connection1.getProtocolInfo().protocolVersion().olderThan(ProtocolVersion.v1_21_11)) return;
                        ClientboundUpdateTagsPacket clientboundUpdateTagsPacket = new ClientboundUpdateTagsPacket();
                        Map<Key, Map<Key, int[]>> tags = clientboundUpdateTagsPacket.getTags();
                        Map<Key, int[]> test = new HashMap<>();
                        test.put(Key.key("minecraft:universal"), new int[]{3});
                        test.put(Key.key("minecraft:in_nether"), new int[]{3});
                        test.put(Key.key("minecraft:in_end"), new int[]{3});
                        test.put(Key.key("minecraft:in_overworld"), new int[]{3,0,2,1});
                        tags.put(Key.key("minecraft:timeline"), test);
                        event.getSession().send(clientboundUpdateTagsPacket);
                        while (!this.tagsUpdateDone);
                    }
                    case ClientboundUpdateTagsPacket clientboundUpdateTagsPacket -> this.tagsUpdateDone = true;
                    default -> {}
                }
            }

            @Override
            public void disconnected(DisconnectedEvent event) {
                event.getCause().printStackTrace();
            }
        });
    }

    @Override
    public void sessionRemoved(SessionRemovedEvent event) {
        if (this.connection != null && this.connection.getHytaleChannel() != null)
            this.connection.getHytaleChannel().close();
        this.connection = null;
    }
}
