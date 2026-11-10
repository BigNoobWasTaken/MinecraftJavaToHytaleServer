package io.github.bignoobwastaken.minecraftjavatohytaleserver.minecraft.network.auth;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.ByteToMessageCodec;

import javax.crypto.Cipher;
import java.util.List;

public class CipherCodec extends ByteToMessageCodec<ByteBuf> {
    private final Cipher cipherI;
    private final Cipher cipherO;
    public CipherCodec(Cipher cipherI, Cipher cipherO) {
        this.cipherI = cipherI;
        this.cipherO = cipherO;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        byte[] encrypted = cipherO.update(bytes);
        out.writeBytes(encrypted);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        byte[] decrypted = cipherI.update(bytes);
        ByteBuf buf = ctx.alloc().buffer().writeBytes(decrypted);
        out.add(buf.retain());
    }
}
