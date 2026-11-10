package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft;

import com.hypixel.hytale.server.core.io.netty.NettyUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;

import java.net.InetSocketAddress;

public class MinecraftServerBootstrap implements AutoCloseable {
    private final EventLoopGroup bossGroup = NettyUtil.getEventLoopGroup(1, "MC-BossGroup");
    private final EventLoopGroup workerGroup = NettyUtil.getEventLoopGroup("MC-WorkerGroup");
    private Channel mcChannel;
    //private final HytaleLogger logger;
    //private final MinecraftServerAdditionalInfo serverInfo;}

    public void init() {
        ServerBootstrap b = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NettyUtil.getServerChannel())
                .option(ChannelOption.SO_BACKLOG, 256)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        //MinecraftClientConnection connection = new MinecraftClientConnection(channel);
                        ChannelPipeline p = channel.pipeline();
                                //.addLast("mc-packet-codec", new MinecraftPacketCodec(connection))
                                //.addLast("mc-packet-handler", new MainPacketHandler(connection));
                    }
                });
        int minecraftPort = 25565;
        InetSocketAddress bindAddress = new InetSocketAddress(minecraftPort);
        try {
            ChannelFuture ch = b.bind(bindAddress).sync();
            if (ch.isSuccess()) {
                mcChannel = ch.channel();
                //this.logger.at(Level.INFO).log("Minecraft TCP listener started on port " + minecraftPort);
            } else  {
                //this.logger.at(Level.INFO).log("Failed to bind Minecraft listener");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        mcChannel.close();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }


}
