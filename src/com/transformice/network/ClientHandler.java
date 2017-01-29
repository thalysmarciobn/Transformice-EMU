package com.transformice.network;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.ParsePackets;
import com.transformice.server.Server;
import org.apache.commons.lang3.ArrayUtils;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class ClientHandler extends SimpleChannelHandler {
    private final Server server;
    private final ParsePackets parse;

    public ClientHandler(Server server) {
        this.server = server;
        this.parse = new ParsePackets(this.server.users);
    }

    @Override
    public void channelOpen(ChannelHandlerContext context, ChannelStateEvent e) {
        this.server.users.sessions.addSession(context.getChannel());
    }

    @Override
    public void channelClosed(ChannelHandlerContext context, ChannelStateEvent e) {
        this.server.users.sessions.removeSession(context.getChannel());
    }

    @Override
    public void messageReceived(ChannelHandlerContext context, MessageEvent e) {
        if ((e.getMessage() instanceof byte[])) {
            byte[] buff = (byte[]) e.getMessage();
            if (buff != null && buff.length > 2) {
                if (this.server.users.sessions.checkIncompletePacket(context.getChannel())) {
                    buff = ArrayUtils.addAll(this.server.users.sessions.getIncompletePacket(context.getChannel()), buff);
                }
                this.parsePacket(context, new ByteArray(buff), buff);
            }
        }
    }

    public void parsePacket(ChannelHandlerContext context, ByteArray packet, byte[] buff) {
        if (packet.size() > 2) {
            byte sizeBytes = packet.readByte();
            int length = sizeBytes == 1 ? packet.readUnsignedByte() : sizeBytes == 2 ? packet.readUnsignedShort() : sizeBytes == 3 ? ((packet.readUnsignedByte() & 0xFF) << 16) | ((packet.readUnsignedByte() & 0xFF) << 8) | (packet.readUnsignedByte() & 0xFF) : 0;
            if (length != 0) {
                byte packetID = packet.readByte();
                if (packet.size() == length) {
                    if (packet.size() >= 2) {
                        this.parse.parsePacket(context.getChannel(), packet, packetID);
                    }
                } else if (packet.size() < length) {
                    this.server.users.sessions.putIncompletePacket(context.getChannel(), buff);
                } else if (packet.size() > length) {
                    byte[] data = packet.read(new byte[length]);
                    if (length >= 2) {
                        this.parse.parsePacket(context.getChannel(), new ByteArray(data), packetID);
                    }
                    if (packet.size() >= 2) {
                        this.parsePacket(context, packet, packet.toByteArray());
                    }
                }
            }
        }
    }
}
