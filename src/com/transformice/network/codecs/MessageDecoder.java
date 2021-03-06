package com.transformice.network.codecs;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class MessageDecoder extends FrameDecoder {
    @Override
    protected Object decode(ChannelHandlerContext Context, Channel channel, ChannelBuffer buffer) throws Exception {
        if (buffer.readableBytes() < 2) {
            return null;
        }
        byte[] buff = new byte[buffer.readableBytes()];
        buffer.readBytes(buff);
        if (new String(buff).startsWith("<policy-file-request/>")) {
            buffer.discardReadBytes();
            channel.write("<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"*\" /></cross-domain-policy>").addListener(ChannelFutureListener.CLOSE);
            return null;
        } else {
            return buff;
        }
    }
}
